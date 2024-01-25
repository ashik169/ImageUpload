package com.ashik.imageupload.ui.component

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ashik.imageupload.utils.FileUtils
import com.ashik.imageupload.utils.ImageCache
import com.ashik.imageupload.utils.fileSizeInMb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class ImageUploadViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) :
    AndroidViewModel(application) {
    companion object {
        const val TAG = "ImageUploadViewModel"

        private const val IMAGE_URI = "image_uri"
    }

    private val imageCache = ImageCache.getInstance()

    val fileInfo = savedStateHandle.getStateFlow<Uri?>(IMAGE_URI, null).mapNotNull { uri ->
        Log.d(TAG, "savedStateHandle.image_uri -> $uri")
        uri ?: return@mapNotNull null
        val myApplication = getApplication() as Application
        val fileInfo = FileUtils.getFileInfo(myApplication, uri)?.also { fileInfo ->
            FileUtils.getBitmap(fileInfo.file, fileInfo.file.fileSizeInMb.toInt())?.let {
                imageCache.put(fileInfo.file.absolutePath, it)
            }
        }
        fileInfo
    }.flowOn(Dispatchers.IO)

    fun updateImage(uris: List<Uri>) {
        Log.d(TAG, "updateImage -> $uris")
        savedStateHandle[IMAGE_URI] = uris.first()
    }

    fun clearImage() {
        Log.d(TAG, "clearImage")
        savedStateHandle[IMAGE_URI] = null
    }
}