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
import com.ashik.imageupload.BuildConfig
import com.ashik.imageupload.R
import com.ashik.imageupload.dao.DataRepository
import com.ashik.imageupload.extensions.parcelableArrayList
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.model.FileProgressModel
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FileBackgroundService : Service() {

    companion object {
        private const val TAG = "BackgroundService"

        private const val UPLOAD_CHANNEL_ID = "upload_service"
        private const val UPLOAD_CHANNEL_NAME = "Upload Service"

        private const val UPLOAD_NOTIFICATION_ID = 2

        val FILE_BACKGROUND_PROGRESS = MutableLiveData<FileProgressModel?>()

        const val ACTION_UPLOAD_IMAGES = "${BuildConfig.APPLICATION_ID}.UPLOAD_IMAGES"

        const val ACTION_DELETE_IMAGES = "${BuildConfig.APPLICATION_ID}.DELETE_IMAGES"

        const val IMAGE_URIS = "image_uris"

        fun startUploadService(context: Context, bundle: Bundle) {
            startService(context, bundle, ACTION_UPLOAD_IMAGES)
        }

        fun startDeleteService(context: Context, bundle: Bundle) {
            startService(context, bundle, ACTION_DELETE_IMAGES)
        }

        private fun startService(context: Context, bundle: Bundle, intentAction: String) {
            val serviceIntent = Intent(
                context, FileBackgroundService::class.java
            ).putExtras(bundle).apply { action = intentAction }
            ContextCompat.startForegroundService(context, serviceIntent)
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
        val imageUris = intent?.extras?.parcelableArrayList<FileInfoModel>(IMAGE_URIS)?.toList()
        if (!imageUris.isNullOrEmpty()) {
            when (intent.action) {
                ACTION_UPLOAD_IMAGES -> uploadImages(imageUris)
                ACTION_DELETE_IMAGES -> deleteImages(imageUris)
            }
        } else {
            stopSelf()
            stopForegroundService()
        }
        return START_STICKY
    }

    private fun uploadImages(list: List<FileInfoModel>) {
        coroutineScope.launch {
            for (i in list.indices) {
                val infoModel = list[i]
                updateNotification(action = ACTION_UPLOAD_IMAGES, index = i, totalCount = list.size)
                val uploadImage = repository.uploadImage(infoModel)
                Log.i(TAG, "uploadImage -> ${uploadImage.isSuccess} -> $infoModel")
            }
            updateNotification(
                action = ACTION_UPLOAD_IMAGES, index = 0, totalCount = list.size, isDone = true
            )
            delay(2000)
            stopSelf()
            stopForegroundService()
        }
    }

    private fun deleteImages(list: List<FileInfoModel>) {
        Log.i(TAG, "deleteImages -> $list")
        coroutineScope.launch {
            for (i in list.indices) {
                val infoModel = list[i]
                updateNotification(action = ACTION_DELETE_IMAGES, index = i, totalCount = list.size)
                val uploadImage = repository.deleteImage(infoModel)
                Log.i(TAG, "deleteImages -> ${uploadImage.isSuccess}")
            }
            updateNotification(
                action = ACTION_DELETE_IMAGES, index = 0, totalCount = list.size, isDone = true
            )
            delay(2000)
            stopSelf()
            stopForegroundService()
        }
    }

    private fun updateNotification(
        action: String, index: Int, totalCount: Int, isDone: Boolean = false
    ) {
        val isUpload = action == ACTION_UPLOAD_IMAGES
        val progressModel = FileProgressModel(
            action = action, totalFile = totalCount, fileIndex = index + 1, isDone = isDone
        )
        val builder = NotificationCompat.Builder(this, UPLOAD_CHANNEL_ID).apply {
            setSmallIcon(R.mipmap.ic_launcher)
            priority = NotificationCompat.PRIORITY_HIGH
            setContentTitle(getString(R.string.app_name))
            if (!isDone) {
                val progress = (((index + 1).toDouble() / totalCount.toDouble()) * 100.0).toInt()
                progressModel.progress = progress
                val message = if (isUpload) getString(
                    R.string.uploading_progress, "${progress}%"
                ) else getString(R.string.deleting_progress, "${progress}%")
                setContentText(message)
                setProgress(100, progress, false)
                setSilent(true)
            } else {
                val message =
                    if (isUpload) getString(R.string.image_upload_success) else getString(R.string.image_delete_success)
                setContentText(message)
            }
        }
        FILE_BACKGROUND_PROGRESS.postValue(progressModel)
        if (ActivityCompat.checkSelfPermission(
                this@FileBackgroundService, Manifest.permission.POST_NOTIFICATIONS
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
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.uploading_images)).setColor(
                    MaterialColors.getColor(
                        applicationContext,
                        com.google.android.material.R.attr.colorPrimary,
                        Color.BLUE
                    )
                ).setOngoing(true).build()
        startForeground(UPLOAD_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        FILE_BACKGROUND_PROGRESS.postValue(null)
        super.onDestroy()
        job.cancel()
    }

    private fun stopForegroundService() {
        @Suppress("DEPRECATION") if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(
            STOP_FOREGROUND_REMOVE
        ) else stopForeground(true)
    }
}