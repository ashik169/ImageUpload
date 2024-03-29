package com.ashik.imageupload.utils

import android.Manifest
import android.os.Build

object Constants {

    const val MAX_IMAGE_UPLOAD = 10
    const val MAX_IMAGE_SHARE = 10

    val READ_STORAGE_PERMISSION: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE
}