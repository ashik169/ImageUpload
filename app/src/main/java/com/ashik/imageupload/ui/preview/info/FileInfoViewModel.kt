package com.ashik.imageupload.ui.preview.info

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ashik.imageupload.model.FileAttribute
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.model.ResultState
import com.ashik.imageupload.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FileInfoViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val fileInfo: StateFlow<ResultState<List<FileAttribute>>> =
        savedStateHandle.getStateFlow<FileInfoModel?>(FileInfoDialogFragment.FILE_INFO, null)
            .map { fileInfo ->
                if (fileInfo == null) return@map ResultState.Error(NullPointerException("No File info "))
                val attributes = mutableListOf(
                    FileAttribute("FileName", fileInfo.fileName),
                    FileAttribute("Location", fileInfo.file.absolutePath)
                )
                fileInfo.fileSize?.takeIf { it > 0L }
                    ?.let { attributes.add(FileAttribute("Size", FileUtils.fileSize(it))) }
                fileInfo.createdDate?.takeIf(String::isNotEmpty)
                    ?.let { attributes.add(FileAttribute("Created Date", it)) }
                fileInfo.lastModified?.takeIf(String::isNotEmpty)
                    ?.let { attributes.add(FileAttribute("Modified Date", it)) }
                ResultState.Success(attributes)
            }.flowOn(Dispatchers.IO).catch {
                emit(ResultState.Success(mutableListOf()))
            }.stateIn(viewModelScope, SharingStarted.Lazily, ResultState.Loading())
}