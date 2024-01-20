package com.ashik.imageupload.utils

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.ashik.imageupload.BuildConfig
import com.ashik.imageupload.extensions.createCacheImageFile
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.utils.FileUtils.APP_AUTHORITY
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.DecimalFormat
import kotlin.io.path.Path
import kotlin.math.log10
import kotlin.math.pow

inline val Uri.isAppLocalDocument: Boolean
    get() = APP_AUTHORITY == authority

/**
 * @return Whether the Uri authority is MediaProvider.
 */
inline val Uri.isMediaDocument: Boolean
    get() = "com.android.providers.media.documents" == authority

/**
 * @return Whether the Uri authority is Google Photos.
 */
inline val Uri.isGooglePhotosUri: Boolean
    get() = "com.google.android.apps.photos.contentprovider" == authority

/**
 * @return Whether the Uri authority is Google Drive.
 */
inline val Uri.isGoogleDriveUri: Boolean
    get() = "com.google.android.apps.docs.storage.legacy" == authority || "com.google.android.apps.docs.storage" == authority

/**
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
inline val Uri.isExternalStorageDocument: Boolean
    get() = "com.android.externalstorage.documents" == authority

object FileUtils {
    private const val TAG = "FileUtils"

    const val APP_AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"
    const val CLOUD_DIR_NAME = "cloud"

    fun getRealPathFromUri(context: Context, uri: Uri): String? {
        Log.d(TAG, "getLocalPath: $uri")
        if (uri.isAppLocalDocument) return uri.toString()
        if (DocumentsContract.isDocumentUri(context, uri)) {
            when {
                uri.isMediaDocument -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])
                    Log.i(
                        TAG, """MediaDocument:Uri -> $uri
                        |split -> $split
                        |contentUri -> $contentUri
                    """.trimMargin()
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }

                uri.isExternalStorageDocument -> {
                    val destinationFile = context.createCacheImageFile
                    saveFileFromUri(context, uri, destinationFile.absolutePath)
                    return destinationFile.absolutePath
                }
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return if (uri.isGooglePhotosUri) {
                val destinationFile = context.createCacheImageFile
                saveFileFromUri(context, uri, destinationFile.absolutePath)
                destinationFile.toString()
            } else if (uri.isGoogleDriveUri) {
                val destinationFile = context.createCacheImageFile
                saveFileFromUri(context, uri, destinationFile.absolutePath)
                destinationFile.toString()
            } else getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = MediaStore.Files.FileColumns.DATA
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(column))
            }
        } catch (e: Exception) {
            Log.e("TAG", e.message.toString())
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String) {
        val buffer = ByteArray(2048)
        val outputStream = FileOutputStream(destinationPath)
        context.contentResolver.openInputStream(uri)?.use { fileIn ->
            outputStream.use { fileOut ->
                while (true) {
                    val length = fileIn.read(buffer)
                    if (length <= 0)
                        break
                    fileOut.write(buffer, 0, length)
                }
                fileOut.close()
            }
        }
        /*var inputStream: InputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            bos = BufferedOutputStream(FileOutputStream(destinationPath, false))
            val buf = ByteArray(1024)
            inputStream!!.read(buf)
            do {
                bos.write(buf)
            } while (inputStream.read(buf) != -1)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
                bos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }*/
    }

    @Throws(Exception::class)
    fun saveCompressImage(context: Context, uri: Uri, destinationPath: String) {
        val options = BitmapFactory.Options().apply {
            inSampleSize = 2
        }
        val scheme = uri.scheme
        Log.i(TAG, "scheme -> $scheme")
        val inputBitmap = if (scheme == "file") {
            val srcFile = uri.toFile()
            BitmapFactory.decodeFile(srcFile.absolutePath, options)
                ?: throw NullPointerException("Bitmap not found -> $srcFile")
        } else {
            context.contentResolver.openInputStream(uri).use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: throw NullPointerException("Bitmap not found")
        }

        FileOutputStream(destinationPath).use {
            inputBitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
            it.flush()
        }
    }

    fun getBitmap(file: File): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 1
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
                ?: throw NullPointerException("Bitmap not found -> $file")
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getFileInfo(file: File): FileInfoModel {
        val fileInfoModel: FileInfoModel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val attrs = Files.readAttributes(
                    Path(file.absolutePath), BasicFileAttributes::class.java
                )
                FileInfoModel(
                    uri = file.toUri(),
                    file = file,
                    fileName = file.name,
                    fileSize = attrs.size(),
                    createdDate = DateUtil.getUIDateTimeFormat(
                        attrs.creationTime().toMillis()
                    ),
                    lastModified = DateUtil.getUIDateTimeFormat(file.lastModified())
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return fileInfoModel ?: FileInfoModel(
            uri = file.toUri(),
            file = file,
            fileName = file.name,
            fileSize = file.length(),
            lastModified = DateUtil.getUIDateTimeFormat(file.lastModified())
        )
    }

    fun fileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / 1024.0.pow(digitGroups.toDouble())
        ) + " " + units[digitGroups]
    }
}

