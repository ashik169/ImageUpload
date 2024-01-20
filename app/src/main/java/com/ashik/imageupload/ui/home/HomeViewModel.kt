package com.ashik.imageupload.ui.home

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ashik.imageupload.dao.DataRepository
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.model.ResultState
import com.ashik.imageupload.utils.DateUtil
import com.ashik.imageupload.utils.FileUtils
import com.ashik.imageupload.utils.ImageCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.Path

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = DataRepository.getInstance(application)

    private val imageCache = ImageCache.getInstance()

    private val _files = MutableStateFlow<ResultState<List<FileInfoModel>>>(ResultState.Loading())
    val files = _files.asStateFlow()

    init {
        fetchImages()
    }

    fun fetchImages() {
        viewModelScope.launch {
            _files.value = ResultState.Loading()
            val fileList = repository.getCloudFiles().map(::getFileInfo)
            withContext(Dispatchers.IO) {
                fileList.forEach { fileInfo ->
                    FileUtils.getBitmap(fileInfo.file)?.let {
                        imageCache.put(fileInfo.file.absolutePath, it)
                    }
                }
            }
            _files.value = ResultState.Success(fileList)
        }
    }

    private fun getFileInfo(file: File): FileInfoModel {
        val fileInfoModel: FileInfoModel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val attrs = Files.readAttributes(
                    Path(file.absolutePath), BasicFileAttributes::class.java
                )
                Log.i(
                    "HomeViewModel", """Size => ${attrs.size()}
                            |Directory => ${attrs.isDirectory}
                            |Link => ${attrs.isSymbolicLink}
                            |Created Date => ${attrs.creationTime()}
                            |Last Modified => ${attrs.lastModifiedTime()}
                        """.trimMargin()
                )
                FileInfoModel(
                    uri = file.toUri(),
                    file = file,
                    fileName = file.name,
                    fileSize = attrs.size(),
                    createdDate = DateUtil.getUIDateTimeFormat(
                        attrs.creationTime().toMillis()
                    ),
                    lastModified = DateUtil.getUIDateTimeFormat(file.lastModified())
                )
            } catch (_: Exception) {
            }
        }
        return fileInfoModel ?: FileInfoModel(
            uri = file.toUri(),
            file = file,
            fileName = file.name,
            fileSize = file.length(),
            lastModified = DateUtil.getUIDateTimeFormat(file.lastModified())
        )
    }
}