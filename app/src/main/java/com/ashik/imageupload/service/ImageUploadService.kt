package com.ashik.imageupload.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ashik.imageupload.R
import com.ashik.imageupload.utils.FileUtils
import com.google.android.material.color.MaterialColors
import createCloudFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageUploadService : Service() {

    companion object {
        private const val TAG = "ImageUploadService"

        private const val UPLOAD_CHANNEL_ID = "upload_service"
        private const val UPLOAD_CHANNEL_NAME = "Upload Service"

        const val IMAGE_URIS = "image_uris"

        private const val UPLOAD_NOTIFICATION_ID = 2

        fun startService(packageContext: Context, bundle: Bundle) {
            val serviceIntent = Intent(
                packageContext,
                ImageUploadService::class.java
            ).apply {
                putExtras(bundle)
            }
            ContextCompat.startForegroundService(packageContext, serviceIntent)
        }
    }

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + job)

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                UPLOAD_CHANNEL_ID, UPLOAD_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
//        TODO("Return the communication channel to the service.")
        return null
    }

    @Suppress("DEPRECATION")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showForeground()
        val uris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.extras?.getParcelableArrayList(IMAGE_URIS, Uri::class.java)
        } else intent?.extras?.getParcelableArrayList(IMAGE_URIS)
        if (!uris.isNullOrEmpty()) {
            uploadImages(uris.toList())
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_STICKY
    }

    private fun uploadImages(uris: List<Uri>) {
        Log.i(TAG, "uploadImages -> $uris")
        coroutineScope.launch {
            for (i in uris.indices) {
                val uri = uris[i]
                val cloudFile = applicationContext.createCloudFile(uri.lastPathSegment)
                Log.i(TAG, "cloudFile -> $cloudFile")
                showNotificationProgress(i, uris.size, false)
                withContext(Dispatchers.IO) {
                    FileUtils.saveFileFromUri(
                        this@ImageUploadService, uri, cloudFile.absolutePath
                    )
                }
            }
            showNotificationProgress(0, uris.size, true)
            stopSelf()
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    private fun showNotificationProgress(index: Int, totalCount: Int, isDone: Boolean) {
        val builder = NotificationCompat.Builder(this, UPLOAD_CHANNEL_ID).apply {
            setSmallIcon(R.mipmap.ic_launcher)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentTitle(getString(R.string.app_name))
            if (isDone) {
                setContentText(getString(R.string.image_upload_success))
            } else {
                val uploadProgress = ((index + 1) / totalCount) * 100
                setContentText(getString(R.string.uploading_progress, "${uploadProgress}%"))
                setProgress(100, uploadProgress, false)
            }
        }
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this@ImageUploadService, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManagerCompat.notify(UPLOAD_NOTIFICATION_ID, builder.build())
        }
    }

    private fun showForeground() {
        val notification =
            NotificationCompat.Builder(this, UPLOAD_CHANNEL_ID).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name)).setContentText("Image Uploading...")
                .setColor(
                    MaterialColors.getColor(
                        applicationContext,
                        com.google.android.material.R.attr.colorPrimary,
                        Color.BLUE
                    )
                ).setOngoing(true).build()
        startForeground(UPLOAD_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}