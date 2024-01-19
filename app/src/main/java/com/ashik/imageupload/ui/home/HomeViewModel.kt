package com.ashik.imageupload.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ashik.imageupload.dao.DataRepository
import com.ashik.imageupload.model.ResultState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = DataRepository(application)

    private val _files = MutableStateFlow<ResultState<List<File>>>(ResultState.Loading())
    val files = _files.asStateFlow()

    fun fetchImages() {
        viewModelScope.launch {
            _files.value = ResultState.Success(repository.getFiles())
        }
    }
}