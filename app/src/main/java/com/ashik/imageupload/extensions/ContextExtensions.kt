package com.ashik.imageupload.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.ashik.imageupload.utils.FileUtils
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