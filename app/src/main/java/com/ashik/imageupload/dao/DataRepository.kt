package com.ashik.imageupload.dao

import android.app.Application
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date

class DataRepository(private val application: Application) {
    suspend fun getFiles(): List<File> = withContext(Dispatchers.IO) {
        Log.i("DataRepository", "getFiles")
        val files = mutableListOf<File>()
        application.filesDir.walkTopDown().forEach {
            Log.i("DataRepository", "topDown -> $it -> ${Date(it.lastModified())}")
            if (!it.isDirectory && it.extension.isNotEmpty()) files.add(it)
        }
        files.sortedByDescending(File::lastModified)
    }
}