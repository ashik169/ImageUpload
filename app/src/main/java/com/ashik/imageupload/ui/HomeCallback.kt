package com.ashik.imageupload.ui

interface HomeCallback {
    val isLoading: Boolean
    fun showLoading()
    fun hideLoading()
}