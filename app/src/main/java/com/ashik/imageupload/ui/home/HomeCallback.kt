package com.ashik.imageupload.ui.home

import com.ashik.imageupload.model.FileInfoModel

interface HomeCallback {
    val isLoading: Boolean
    fun showLoading()
    fun hideLoading()
    fun updateSubtitle(value: String?)
    fun shareFile(fileInfoModel: FileInfoModel)
    fun shareFiles(selectedItems: List<FileInfoModel>)
}