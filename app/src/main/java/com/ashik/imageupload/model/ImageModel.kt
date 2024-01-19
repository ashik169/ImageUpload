package com.ashik.imageupload.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageModel(
    val contentUri: Uri,
    val fileUri: Uri,
    val fileName: String,
): Parcelable