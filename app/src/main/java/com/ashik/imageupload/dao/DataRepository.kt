package com.ashik.imageupload.dao

import android.app.Application
import android.util.Log
import com.ashik.imageupload.extensions.createCloudFile
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File


class DataRepository private constructor(private val application: Application) {

    companion object {

        private const val TAG = "DataRepository"

        @Volatile
        private var instance: DataRepository? = null

        fun getInstance(application: Application) =
            instance ?: synchronized(this) {
                instance ?: DataRepository(application).also { instance = it }
            }
    }

    suspend fun getCloudFiles(): List<File> = withContext(Dispatchers.IO) {
        val filesDir = application.filesDir
        val cloudDir = File(filesDir, FileUtils.CLOUD_DIR_NAME)
        cloudDir.walkTopDown().filter {
            !it.isDirectory && it.extension.isNotEmpty()
        }.sortedByDescending(File::lastModified).toList()
    }

    suspend fun uploadImage(fileInfoModel: FileInfoModel): Result<Boolean> =
        withContext(Dispatchers.IO) {
            delay(500)
            try {
                val srcFile = fileInfoModel.file
                val cloudFile = application.createCloudFile(fileInfoModel.fileName).apply {
                    setLastModified(srcFile.lastModified())
                }
                Log.d(
                    TAG, """cloudFile -> $cloudFile
                        |fileInfoModel -> $fileInfoModel
                    """.trimMargin()
                )
                FileUtils.saveCompressImage(application, fileInfoModel.uri, cloudFile.absolutePath)
                Result.success(true)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }


    suspend fun deleteImage(fileInfoModel: FileInfoModel): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(fileInfoModel.file.delete())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}