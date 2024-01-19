package com.ashik.imageupload.ui.preview

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ashik.imageupload.model.ResultState
import com.ashik.imageupload.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PreviewViewModel(application: Application, savedStateHandle: SavedStateHandle) :
    AndroidViewModel(application) {

    private val _uriStateFlow = MutableStateFlow<ResultState<List<Uri>>>(ResultState.Loading())
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
                savedStateHandle.get<ArrayList<Uri>>(PreviewFragment.IMAGE_URIS)
                    ?.mapNotNull {
                        /*application.contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )*/

                        withContext(Dispatchers.IO) {
                            try {
                                val filePath = FileUtils.getFilePath(myApplication, it)
                                val toUri =
                                    if (!filePath.isNullOrEmpty()) File(filePath).toUri() else null

                                Log.d(
                                    PreviewFragment.TAG, """Uri -> $it
                                | File Path -> $filePath
                                | toUri -> $toUri
                            """.trimMargin()
                                )
                                toUri
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