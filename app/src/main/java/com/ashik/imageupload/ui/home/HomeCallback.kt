package com.ashik.imageupload.ui.home

interface HomeCallback {
    val isLoading: Boolean
    fun showLoading()
    fun hideLoading()
}