package com.ashik.imageupload.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ashik.imageupload.dao.DataRepository
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.model.ResultState
import com.ashik.imageupload.utils.FileUtils
import com.ashik.imageupload.utils.ImageCache
import com.ashik.imageupload.utils.fileSizeInMb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = DataRepository.getInstance(application)

    private val imageCache = ImageCache.getInstance()

    private val _files = MutableStateFlow<ResultState<List<FileInfoModel>>>(ResultState.Loading())
    val files = _files.asStateFlow()

    private val _deleteFile = MutableSharedFlow<Result<Int>>()
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
                    val file = fileInfo.file
                    val fileSizeInMb = file.fileSizeInMb
                    FileUtils.getBitmap(file, fileSizeInMb.toInt())?.let {
                        fileInfo.dimension = "${it.width} * ${it.height}"
                        imageCache.put(file.absolutePath, it)
                    }
                    FileUtils.getBitmap(file, 4)?.let {
                        fileInfo.dimension = "${it.width} * ${it.height}"
                        imageCache.put(fileInfo.thumbnailKey, it)
                    }
                }
            }
            _files.value = ResultState.Success(fileList)
        }
    }

    fun deleteImage(fileInfoModel: FileInfoModel, currentItem: Int) {
        viewModelScope.launch {
            val result = repository.deleteImage(fileInfoModel)
            if (result.isSuccess) {
                _deleteFile.emit(Result.success(currentItem))
                delay(300)
                val resultState = _files.value
                if (resultState is ResultState.Success) {
                    val mutableList = resultState.data.toMutableList().also {
                        it.remove(fileInfoModel)
                    }
                    _files.update {
                        ResultState.Success(mutableList)
                    }
                }
            } else {
                _deleteFile.emit(
                    Result.failure(
                        result.exceptionOrNull() ?: Exception("Failed to delete image")
                    )
                )
            }
        }
    }
}