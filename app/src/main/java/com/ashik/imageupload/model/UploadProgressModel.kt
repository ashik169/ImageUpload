package com.ashik.imageupload.model

data class UploadProgressModel(
    val totalFile: Int = 0,
    val fileIndex: Int = 0,
    var progress: Int = 0,
    val isDone: Boolean = false
)