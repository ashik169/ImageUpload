package com.ashik.imageupload.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
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

inline val File.fileSizeInMb: Float
    get() = length().toFloat() / (1024f * 1024f)

object FileUtils {
    private const val TAG = "FileUtils"

    const val APP_AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"
    const val CLOUD_DIR_NAME = "cloud"

    @Throws(Exception::class)
    fun getRealPathFromUri(context: Context, uri: Uri): String {
        Log.d(TAG, "getLocalPath: $uri")
        var filePath: String? = if (uri.isAppLocalDocument) uri.toString()
        else if (uri.isGooglePhotosUri || uri.isGoogleDriveUri || uri.isExternalStorageDocument) {
            saveToCacheLocation(context, uri)
        } else if (DocumentsContract.isDocumentUri(context, uri) && uri.isMediaDocument) {
            val split = DocumentsContract.getDocumentId(uri).split(":".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
            Log.d(TAG, "MediaDocument Id -> ${split.joinToString { it }}")
            getMediaDataLocation(
                context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "_id=?", arrayOf(split[1])
            )
        } else if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            getMediaDataLocation(context, uri, null, null)
        } else if (ContentResolver.SCHEME_FILE == uri.scheme) uri.path else null

        if (filePath.isNullOrEmpty()) {
            filePath = saveToCacheLocation(context, uri)
        }
        return filePath
    }

    private fun saveToCacheLocation(context: Context, uri: Uri): String {
        val docFileName = getDocumentFileName(context, uri)
        Log.d(TAG, "External:Uri -> $uri -> $docFileName")
        val destinationFile = context.createCacheImageFile(docFileName)
        saveFileFromUri(context, uri, destinationFile.absolutePath)
        return destinationFile.absolutePath
    }

    private fun getMediaDataLocation(
        context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?
    ): String? {
        Log.d(TAG, "Get DataColumn -> $uri, $selection, $selectionArgs")
        return try {
            val column = MediaStore.Files.FileColumns.DATA
            val projection = arrayOf(column)
            context.contentResolver.query(
                uri, projection, selection, selectionArgs, null
            )?.use {
                return@use if (it.moveToFirst()) {
                    it.getString(it.getColumnIndexOrThrow(column))
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getDocumentFileName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(
                uri, null, null, null, null
            )?.use {
                if (it.moveToFirst()) {
                    it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else null
            }
        } catch (e: Exception) {
            Log.e("TAG", e.message.toString())
            return null
        }
    }

    private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String) {
        val buffer = ByteArray(2048)
        val outputStream = FileOutputStream(destinationPath)
        context.contentResolver.openInputStream(uri)?.use { fileIn ->
            outputStream.use { fileOut ->
                while (true) {
                    val length = fileIn.read(buffer)
                    if (length <= 0) break
                    fileOut.write(buffer, 0, length)
                }
                fileOut.close()
            }
        }
    }

    @Throws(Exception::class)
    fun saveCompressImage(context: Context, uri: Uri, destinationPath: String) {
        Log.d(TAG, "saveCompressImage -> $uri -> $destinationPath")
        val outputOptions = BitmapFactory.Options().apply {
            inSampleSize = 1
        }/*val inputBitmap = when (uri.scheme) {
            "file" -> {
                val srcFile = uri.toFile()
                BitmapFactory.decodeFile(srcFile.absolutePath, outputOptions)
                    ?: throw NullPointerException("Bitmap not found -> $srcFile")
            }

            else -> {
                context.contentResolver.openInputStream(uri).use {
                    BitmapFactory.decodeStream(it, null, outputOptions)
                } ?: throw NullPointerException("Bitmap not found")
            }
        }*/
        val inputBitmap = context.contentResolver.openInputStream(uri).use {
            BitmapFactory.decodeStream(it, null, outputOptions)
        } ?: throw NullPointerException("Bitmap not found")

        FileOutputStream(destinationPath).use {
            inputBitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
            it.flush()
        }
    }

    fun getBitmap(file: File, sampleSize: Int = 1): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            BitmapFactory.decodeFile(file.absolutePath, options) ?: throw NullPointerException(
                "Bitmap not found -> $file"
            )
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


    fun getFileInfo(context: Context, uri: Uri): FileInfoModel? {
        try {
            val sPath = getRealPathFromUri(context, uri)
            val filePath = File(sPath)
            val fileUri = if (sPath.isNotEmpty()) filePath.toUri() else return null
            Log.d(
                TAG, """ContentUri -> $uri 
                    |File Path -> $filePath
                    |File Uri -> $fileUri""".trimMargin()
            )
            return FileInfoModel(
                uri = fileUri,
                file = filePath,
                fileName = filePath.name,
                fileSize = filePath.length(),
                lastModified = DateUtil.getUIDateTimeFormat(filePath.lastModified())
            )
        } catch (e: Exception) {
            return null
        }
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

