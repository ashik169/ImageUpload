package com.ashik.imageupload.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.ashik.imageupload.BuildConfig
import com.ashik.imageupload.utils.FileUtils.APP_AUTHORITY
import createImageFile
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

val Uri.isAppLocalDocument: Boolean
    get() = APP_AUTHORITY == authority

/**
 * @return Whether the Uri authority is MediaProvider.
 */
val Uri.isMediaDocument: Boolean
    get() = "com.android.providers.media.documents" == authority


/**
 * @return Whether the Uri authority is Google Photos.
 */
val Uri.isGooglePhotosUri: Boolean
    get() = "com.google.android.apps.photos.contentprovider" == authority

object FileUtils {
    private const val TAG = "FileUtils"

    const val APP_AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"

    fun getFilePath(context: Context, uri: Uri): String? {
        Log.d(TAG, "getLocalPath: $uri")
        if (uri.isAppLocalDocument) return uri.toString()
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (uri.isMediaDocument) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return if (uri.isGooglePhotosUri) {
                val destinationFile = context.createImageFile
                saveFileFromUri(context, uri, destinationFile.absolutePath)
                /*val buffer = ByteArray(2048)
                val outputStream = FileOutputStream(File(context.filesDir, "test.jpg"))
                context.contentResolver.openInputStream(uri)?.use { fileIn ->
                    outputStream.use { fileOut ->
                        while (true) {
                            val length = fileIn.read(buffer)
                            if (length <= 0)
                                break
                            fileOut.write(buffer, 0, length)
                        }
                        fileOut.flush()
                        fileOut.close()
                    }
                }*/
                destinationFile.toUri().toString()
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

    fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String?) {
        var inputStream: InputStream? = null
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
        }
    }
}

