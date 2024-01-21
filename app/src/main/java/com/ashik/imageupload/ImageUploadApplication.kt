package com.ashik.imageupload

import android.app.Application
import com.ashik.imageupload.utils.ImageCache

class ImageUploadApplication: Application() {


    private lateinit var imageCache: ImageCache

    override fun onCreate() {
        super.onCreate()
        imageCache = ImageCache.getInstance()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        imageCache.clearMemoryCache()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        imageCache.clearMemoryCache()
    }
}