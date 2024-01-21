package com.ashik.imageupload.model

import android.os.Bundle


sealed class ResultState<out T> {
    data class Success<out T>(val data: T) : ResultState<T>()

    data class Error(val exception: Exception, val bundle: Bundle? = null) : ResultState<Nothing>()

    data class Loading(val bundle: Bundle? = null) : ResultState<Nothing>()
}