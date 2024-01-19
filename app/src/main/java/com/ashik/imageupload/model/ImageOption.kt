package com.ashik.imageupload.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ashik.imageupload.R

data class ImageOption(
    val id: Int,
    @StringRes val titleResId: Int,
    @DrawableRes val iconResId: Int
) {
    companion object {
        const val CAMERA = 1
        const val GALLERY = 2
        val IMAGE_OPTIONS = listOf(
            ImageOption(
                CAMERA,
                R.string.camera,
                R.drawable.photo_camera_filled_24dp
            ), ImageOption(
                GALLERY,
                R.string.gallery,
                R.drawable.photo_library_filled_24dp
            )
        )
    }
}