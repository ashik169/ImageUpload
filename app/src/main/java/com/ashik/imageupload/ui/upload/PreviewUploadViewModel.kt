package com.ashik.imageupload.ui.upload

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.model.ResultState
import com.ashik.imageupload.utils.DateUtil
import com.ashik.imageupload.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PreviewUploadViewModel(application: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application) {

    private val _uriStateFlow =
        MutableStateFlow<ResultState<List<FileInfoModel>>>(ResultState.Loading())
    val uriStateFlow = _uriStateFlow.asStateFlow()

    /*var uriStateFlow2: StateFlow<ResultState<List<Uri>>> =
        savedStateHandle.getStateFlow<ArrayList<Uri>>(PreviewFragment.IMAGE_URIS, arrayListOf())
            .map { uris ->
                Log.i("PreviewViewModel", "Map Uris -> $uris")
                val myApplication = getApplication() as Application
                val mappedList =
                    uris.mapNotNull { FileUtils.getFilePath(myApplication, it)?.toUri() }
                ResultState.Success(mappedList)
            }.flowOn(Dispatchers.IO).catch {
                Log.e("PreviewViewModel", it.message.toString())
                emit(ResultState.Success(listOf()))
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, ResultState.Loading())*/

    init {
        viewModelScope.launch {
            val myApplication = getApplication() as Application
            val uriList =
                savedStateHandle.get<ArrayList<Uri>>(PreviewUploadFragment.IMAGE_URIS)
                    ?.mapNotNull {
                        withContext(Dispatchers.IO) {
                            delay(300)
                            try {
                                val sPath = FileUtils.getRealPathFromUri(myApplication, it)
                                    ?: return@withContext null
                                val filePath = File(sPath)
                                val toUri =
                                    if (sPath.isNotEmpty()) filePath.toUri() else return@withContext null
                                Log.d(
                                    PreviewUploadFragment.TAG, """Uri -> $it
                                | File Path -> $filePath
                                | toUri -> $toUri
                            """.trimMargin()
                                )
                                FileInfoModel(
                                    uri = toUri,
                                    file = filePath,
                                    fileName = filePath.name,
                                    fileSize = filePath.length(),
                                    lastModified = DateUtil.getUIDateTimeFormat(filePath.lastModified())
                                )
                            } catch (e: Exception) {
                                return@withContext null
                            }
                        }
                    }
            when (uriList) {
                null -> _uriStateFlow.value = ResultState.Success(listOf())
                else -> _uriStateFlow.value = ResultState.Success(uriList)
            }
            /*savedStateHandle.getStateFlow<ArrayList<Uri>>(
                PreviewFragment.IMAGE_URIS,
                arrayListOf()
            ).map { uris ->
                val myApplication = getApplication() as Application
                uris.mapNotNull { FileUtils.getFilePath(myApplication, it)?.toUri() }
            }.flowOn(Dispatchers.IO).catch {
                _uriStateFlow.value = ResultState.Error(Exception(it))
            }.collectLatest {
                _uriStateFlow.value = ResultState.Success(it)
            }*/
        }
    }
}