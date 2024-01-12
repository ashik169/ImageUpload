package com.ashik.imageupload.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import createImageFile
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object FileUtils {
    const val TAG = "FileUtils"

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.contentprovider" == uri.authority
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

    fun getFilePath(context: Context, uri: Uri): String? {
        Log.d(TAG, "getLocalPath: $uri")
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } 
        else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return if (isGooglePhotosUri(uri)) {
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
            }
            else getDataColumn(context, uri, null, null)
        } 
        else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String?) {
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
