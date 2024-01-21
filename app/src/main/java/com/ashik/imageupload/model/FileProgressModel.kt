package com.ashik.imageupload.model

data class FileProgressModel(
    val action: String,
    val totalFile: Int = 0,
    val fileIndex: Int = 0,
    var progress: Int = 0,
    val isDone: Boolean = false
)