package com.ashik.imageupload.ui.upload

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.model.ResultState
import com.ashik.imageupload.utils.FileUtils
import com.ashik.imageupload.utils.ImageCache
import com.ashik.imageupload.utils.fileSizeInMb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreviewUploadViewModel(application: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application) {

    private val imageCache = ImageCache.getInstance()
    private val _uriStateFlow =
        MutableStateFlow<ResultState<List<FileInfoModel>>>(ResultState.Loading())
    val uriStateFlow = _uriStateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            val myApplication = getApplication() as Application
            val uriList = savedStateHandle.get<ArrayList<Uri>>(PreviewUploadFragment.IMAGE_URIS)
                ?.mapNotNull { uri ->
                    withContext(Dispatchers.IO) {
                        val fileInfo = FileUtils.getFileInfo(myApplication, uri)?.also { fileInfo ->
                            FileUtils.getBitmap(fileInfo.file, fileInfo.file.fileSizeInMb.toInt())
                                ?.let {
                                    imageCache.put(fileInfo.file.absolutePath, it)
                                }
                        }
                        fileInfo
                    }
                } ?: listOf()
            _uriStateFlow.value = ResultState.Success(uriList)
        }
    }
}