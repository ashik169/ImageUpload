package com.ashik.imageupload.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.ashik.imageupload.R
import com.ashik.imageupload.dao.DataRepository
import com.ashik.imageupload.extensions.parcelableArrayList
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.model.UploadProgressModel
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ImageUploadService : Service() {

    companion object {
        private const val TAG = "ImageUploadService"

        private const val UPLOAD_CHANNEL_ID = "upload_service"
        private const val UPLOAD_CHANNEL_NAME = "Upload Service"

        const val IMAGE_URIS = "image_uris"

        private const val UPLOAD_NOTIFICATION_ID = 2

        val UPLOAD_PROGRESS = MutableLiveData<UploadProgressModel?>()

        fun startService(packageContext: Context, bundle: Bundle) {
            val serviceIntent = Intent(
                packageContext, ImageUploadService::class.java
            ).apply {
                putExtras(bundle)
            }
            ContextCompat.startForegroundService(packageContext, serviceIntent)
        }
    }

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + job)

    private lateinit var repository: DataRepository

    override fun onCreate() {
        super.onCreate()
        repository = DataRepository.getInstance(application)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                UPLOAD_CHANNEL_ID, UPLOAD_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onBind(intent: Intent) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showForeground()
        val uris = intent?.extras?.parcelableArrayList<FileInfoModel>(IMAGE_URIS)
        if (!uris.isNullOrEmpty()) {
            uploadImages(uris.toList())
        } else {
            stopSelf()
            stopForegroundService()
        }
        return START_STICKY
    }

    private fun uploadImages(list: List<FileInfoModel>) {
        Log.i(TAG, "uploadImages -> $list")
        coroutineScope.launch {
            for (i in list.indices) {
                val infoModel = list[i]
                updateNotification(i, list.size)
                val uploadImage = repository.uploadImage(infoModel)
                Log.i(TAG, "uploadImage -> ${uploadImage.isSuccess}")
            }
            updateNotification(0, list.size, true)
            delay(2000)
            stopSelf()
            stopForegroundService()
        }
    }

    private fun updateNotification(index: Int, totalCount: Int, isDone: Boolean = false) {
        val uploadProgressModel =
            UploadProgressModel(totalFile = totalCount, fileIndex = index + 1, isDone = isDone)
        val builder = NotificationCompat.Builder(this, UPLOAD_CHANNEL_ID).apply {
            setSmallIcon(R.mipmap.ic_launcher)
            priority = NotificationCompat.PRIORITY_HIGH
            setContentTitle(getString(R.string.app_name))
            if (isDone) {
                setContentText(getString(R.string.image_upload_success))
            } else {
                val uploadProgress = ((index + 1) / totalCount) * 100
                uploadProgressModel.progress = uploadProgress
                setContentText(getString(R.string.uploading_progress, "${uploadProgress}%"))
                setProgress(100, uploadProgress, false)
                setSilent(true)
            }
        }
        UPLOAD_PROGRESS.postValue(uploadProgressModel)
        if (ActivityCompat.checkSelfPermission(
                this@ImageUploadService, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notificationManagerCompat = NotificationManagerCompat.from(this)
            val notificationId =
                if (isDone) ((UPLOAD_NOTIFICATION_ID + 1)..100).random() else UPLOAD_NOTIFICATION_ID
            notificationManagerCompat.notify(notificationId, builder.build())
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
        UPLOAD_PROGRESS.postValue(null)
        super.onDestroy()
        job.cancel()
    }

    private fun stopForegroundService() {
        @Suppress("DEPRECATION") if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(
            STOP_FOREGROUND_REMOVE
        ) else stopForeground(true)
    }
}