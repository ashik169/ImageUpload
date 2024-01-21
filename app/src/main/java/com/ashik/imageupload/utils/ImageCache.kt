package com.ashik.imageupload.utils

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache

class ImageCache private constructor() {

    companion object {

        @Volatile
        private var instance: ImageCache? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: ImageCache().also { instance = it }
            }
    }

    private val cache: LruCache<String, Bitmap>

    init {
        val maxMemory = Runtime.getRuntime().maxMemory() / 1024
        val cacheSize = (maxMemory / 4).toInt()
        Log.d("ImageCache", "cacheSize -> $cacheSize and maxMemory -> $maxMemory")
        cache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return (value.rowBytes) * (value.height) / 1024
            }
        }
    }

    fun put(url: String, bitmap: Bitmap) {
        cache.put(url, bitmap)
    }

    fun get(url: String): Bitmap? = cache.get(url)

    fun contains(url: String): Boolean = cache.get(url) != null

    fun clearMemoryCache() {
        cache.evictAll()
    }
}