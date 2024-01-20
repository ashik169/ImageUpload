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
import com.ashik.imageupload.utils.FileUtils.getFileInfo
import com.ashik.imageupload.utils.ImageCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _deleteFile = MutableSharedFlow<ResultState<Boolean>>()
    val deleteFile = _deleteFile.asSharedFlow()

    init {
        fetchImages()
    }

    fun fetchImages() {
        viewModelScope.launch {
            _files.value = ResultState.Loading()
            val fileList = repository.getCloudFiles().map(FileUtils::getFileInfo)
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

    fun deleteFiles(selectedItems: MutableList<FileInfoModel>) {
        Log.i("HomeViewModel", "deleteFiles -> $selectedItems")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    selectedItems.forEach {
                        val delete = it.file.delete()
                        Log.i("HomeViewModel", "Delete File -> ${it.file} -> $delete")
                    }
                    _deleteFile.emit(ResultState.Success(true))
                } catch (e: Exception) {
                    _deleteFile.emit(ResultState.Success(false))
                }
            }
        }
    }
}