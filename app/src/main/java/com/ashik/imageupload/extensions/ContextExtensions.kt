package com.ashik.imageupload.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ashik.imageupload.R
import com.ashik.imageupload.databinding.PickImageOptionsBinding
import com.ashik.imageupload.model.ImageOption
import com.ashik.imageupload.ui.imageoption.ImageOptionAdapter
import com.ashik.imageupload.utils.Constants
import com.ashik.imageupload.utils.FileUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun Context.showToast(msg: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, msg, duration).show()
}

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this, permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.getUriForFile(file: File): Uri = FileProvider.getUriForFile(
    applicationContext, FileUtils.APP_AUTHORITY, file
)

fun Context.createCacheImageFile(fileName: String? = null): File =
    if (fileName.isNullOrEmpty()) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        File(cacheDir, "IMG_${timeStamp}.jpg")
    } else File(cacheDir, fileName)

@Throws(IOException::class)
fun Context.createCloudFile(fileName: String?): File {
    val cloudDir = File(filesDir, FileUtils.CLOUD_DIR_NAME)
    if (!cloudDir.exists()) cloudDir.mkdirs()
    return if (fileName.isNullOrEmpty()) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        File(cloudDir, "IMG_${timeStamp}.jpg")
    } else File(cloudDir, fileName)
}

inline fun <reified T : Parcelable> Bundle?.parcelableArrayList(key: String): ArrayList<T>? {
    this ?: return null
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableArrayList(
            key, T::class.java
        )

        else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
    }
}

inline fun <reified T : Parcelable> Bundle?.parcelable(key: String): T? {
    this ?: return null
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(
            key, T::class.java
        )

        else -> @Suppress("DEPRECATION") getParcelable(key)
    }
}

fun Context.showChooseOptions(optionCallback: (ImageOption) -> Unit) {
    val imageOptions = ImageOption.IMAGE_OPTIONS
    val bottomSheetDialog = BottomSheetDialog(this)
    val pickImageOptionsBinding =
        PickImageOptionsBinding.inflate(LayoutInflater.from(this)).apply {
            hintMaxImage.text =
                getString(R.string.hint_max_img_allow, Constants.MAX_IMAGE_UPLOAD)
        }
    bottomSheetDialog.setContentView(pickImageOptionsBinding.root)
    val imageOptionAdapter = ImageOptionAdapter { option, _, _ ->
        optionCallback(option)
        bottomSheetDialog.dismiss()
    }
    imageOptionAdapter.submitList(imageOptions)
    pickImageOptionsBinding.rvOptions.apply {
        layoutManager = LinearLayoutManager(context)
//            layoutManager = GridLayoutManager(context, 2)
        adapter = imageOptionAdapter
    }
    bottomSheetDialog.show()
}