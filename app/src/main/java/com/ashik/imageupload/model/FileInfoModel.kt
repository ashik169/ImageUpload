package com.ashik.imageupload.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class FileInfoModel(
    val uri: Uri,
    val file: File,
    val fileName: String,
    val fileSize: Long? = null,
    val createdDate: String? = null,
    val lastModified: String? = null,
    var isSelected: Boolean = false,
) : Parcelable