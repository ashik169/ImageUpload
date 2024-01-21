package com.ashik.imageupload.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ashik.imageupload.extensions.createCacheImageFile
import com.ashik.imageupload.extensions.getUriForFile
import com.ashik.imageupload.extensions.hasPermission
import com.ashik.imageupload.extensions.showToast
import com.ashik.imageupload.utils.Constants
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


interface IMediaResultCallback {

    /*
    * If returns true derived class will be taken responsibility to show messages
    * */
    fun onPermissionDenied(): Boolean

    fun onImageResult(uris: List<Uri>)
}

open class MediaResultContract(
    private val context: Context,
    private val registry: ActivityResultRegistry,
    private val mediaResultCallback: IMediaResultCallback,
) : DefaultLifecycleObserver {

    private lateinit var mediaPermissionResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cameraAppResultLauncher: ActivityResultLauncher<Uri>
    private var galleryResultLauncher: ActivityResultLauncher<Intent>? = null
    private var pickMediaResultLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null

    private val mNextLocalRequestCode = AtomicInteger()
    private val randomRegisterKey: String
        get() = "fragment_rq#${mNextLocalRequestCode.getAndIncrement()}"

    private val isPhotoPickerAvailable
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private var isCameraPermission = false
    private var photoFile: File? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        //        https://developer.android.com/training/basics/intents/result#custom
        //        https://developer.android.com/training/basics/intents/result
        mediaPermissionResultLauncher = registry.register(
            randomRegisterKey, owner, ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionMap ->
            val allPermissionGranted = permissionMap.all(Map.Entry<String, Boolean>::value)
            when {
                allPermissionGranted -> when {
                    isCameraPermission -> openCamera()
                    else -> openGallery()
                }

                else -> {
                    val onPermissionDenied = mediaResultCallback.onPermissionDenied()
                    if (!onPermissionDenied) context.showToast("Permission Required capturing a picture")
                }
            }
        }

        if (isPhotoPickerAvailable) {
            pickMediaResultLauncher = registry.register(
                randomRegisterKey,
                owner,
                ActivityResultContracts.PickMultipleVisualMedia(Constants.MAX_IMAGE_UPLOAD)
            ) { it.takeIf { it.isNotEmpty() }?.let(mediaResultCallback::onImageResult) }
        } else {
            galleryResultLauncher = registry.register(
                randomRegisterKey, owner, ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    val clipData = intent?.clipData
                    val uris = mutableListOf<Uri>()
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            clipData.getItemAt(i).uri
                            uris.add(clipData.getItemAt(i).uri)
                        }
                    } else {
                        intent?.data?.let(uris::add)
                    }
                    if (uris.isNotEmpty()) {
                        if (uris.size > Constants.MAX_IMAGE_UPLOAD) {
                            mediaResultCallback.onImageResult(uris.take(Constants.MAX_IMAGE_UPLOAD))
                        } else {
                            mediaResultCallback.onImageResult(uris)
                        }
                    } else {
                        context.showToast("Failed to pick image from gallery")
                    }
                    Log.e(TAG, "Uris -> $uris")
                } else {
                    Log.e(TAG, "Uri is not picked")
                }
            }
        }

        cameraAppResultLauncher = registry.register(
            randomRegisterKey, owner, ActivityResultContracts.TakePicture()
        ) {
            if (it) {
                photoFile?.let { file ->
                    mediaResultCallback.onImageResult(mutableListOf(file.toUri()))
                }
            }
        }
    }

    fun openCamera() {
        val permissions = mutableListOf<String>()
        Manifest.permission.CAMERA.takeIf { !context.hasPermission(it) }?.let(permissions::add)
        Constants.READ_STORAGE_PERMISSION.takeIf { !context.hasPermission(it) }
            ?.let(permissions::add)
        if (permissions.isEmpty()) {
            photoFile = try {
                val imageFile = context.createCacheImageFile()
                val photoUri = context.getUriForFile(imageFile)
                cameraAppResultLauncher.launch(photoUri)
                imageFile
            } catch (ex: IOException) {
                null
            }
            if (photoFile == null) {
                context.showToast("Error occurred while creating the File")
            }
        } else {
            isCameraPermission = true
            mediaPermissionResultLauncher.launch(permissions.toTypedArray())
        }
    }

    fun openGallery() {
        val storagePermission = Constants.READ_STORAGE_PERMISSION
        if (context.hasPermission(storagePermission)) {
            if (isPhotoPickerAvailable) {
                pickMediaResultLauncher!!.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                val intent = Intent().apply {
                    action = Intent.ACTION_GET_CONTENT
                    type = "image/*"
                }.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                galleryResultLauncher!!.launch(Intent.createChooser(intent, "Select Picture"))
            }
        } else {
            isCameraPermission = false
            mediaPermissionResultLauncher.launch(arrayOf(storagePermission))
        }
    }

    companion object {
        const val TAG = "MediaResultContract"
    }
}