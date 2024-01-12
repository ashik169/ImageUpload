package com.ashik.imageupload.ui

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ashik.imageupload.utils.Constants
import com.ashik.imageupload.R
import com.ashik.imageupload.databinding.FragmentPickImageBinding
import com.ashik.imageupload.utils.FileUtils
import createImageFile
import hasPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException


class PickImageFragment : Fragment() {

    private var _binding: FragmentPickImageBinding? = null

    private val binding get() = _binding!!

    private lateinit var multiplePermissionResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Uri?>
    private var photoPickerResultLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var galleryResultLauncher: ActivityResultLauncher<String>? = null

    private val isPhotoPickerAvailable
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private var isCameraPermission = false

    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initContract()
    }

    private fun initContract() {
        multiplePermissionResultLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                val allGranted = it.all(Map.Entry<String, Boolean>::value)
                when {
                    !allGranted -> {
                        Toast.makeText(
                            requireContext(),
                            "Permission Required capturing a picture",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    isCameraPermission -> onClickCamera()
                    else -> onClickGallery()
                }
            }
        cameraResultLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    photoUri?.let { uri ->
                        findNavController().navigate(
                            R.id.action_navigate_to_upload_image, bundleOf(
                                ImageUploadFragment.IMAGE_PATH to uri
                            )
                        )
                    }
                }
            }

        if (isPhotoPickerAvailable) {
            photoPickerResultLauncher =
                registerForActivityResult(
                    ActivityResultContracts.PickVisualMedia(),
                    ::onImagePicked
                )
        } else {
            galleryResultLauncher =
                registerForActivityResult(ActivityResultContracts.GetContent(), ::onImagePicked)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonGallery.setOnClickListener { onClickGallery() }
        binding.buttonCamera.setOnClickListener { onClickCamera() }
    }

    private fun onClickGallery() {
        val storagePermission = Constants.READ_STORAGE_PERMISSION
        if (requireContext().hasPermission(storagePermission)) {
            if (isPhotoPickerAvailable) {
                photoPickerResultLauncher!!.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                galleryResultLauncher!!.launch("image/*")
            }
        } else {
            isCameraPermission = false
            multiplePermissionResultLauncher.launch(arrayOf(storagePermission))
        }
    }

    private fun onClickCamera() {
        val permissions = mutableListOf<String>()
        if (!requireContext().hasPermission(Manifest.permission.CAMERA)) permissions.add(Manifest.permission.CAMERA)
        Constants.READ_STORAGE_PERMISSION.takeIf { !requireContext().hasPermission(it) }
            ?.let(permissions::add)
        if (permissions.isEmpty()) {
            val photoFile: File? = try {
                requireContext().createImageFile
            } catch (ex: IOException) {
                null
            }
            Log.i("PhotoFile", photoFile.toString())
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.ashik.imageupload.fileprovider",
                    photoFile
                )
                cameraResultLauncher.launch(photoUri)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error occurred while creating the File",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            isCameraPermission = true
            multiplePermissionResultLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun onImagePicked(galleryUri: Uri?) {
        viewLifecycleOwner.lifecycleScope.launch {
            Log.d(FileUtils.TAG, "Gallery Selected URI: $galleryUri")
            if (galleryUri != null) {
                val imagePath = withContext(Dispatchers.IO) {
                    FileUtils.getFilePath(requireContext(), galleryUri)
                }
                Log.d(
                    FileUtils.TAG, """Uri: $galleryUri
                | FilePath -> $imagePath""".trimMargin()
                )
                if (!imagePath.isNullOrEmpty()) {
                    findNavController().navigate(
                        R.id.action_navigate_to_upload_image, bundleOf(
                            ImageUploadFragment.IMAGE_PATH to imagePath.toUri()
                        )
                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Image path not found",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        multiplePermissionResultLauncher.unregister()
        cameraResultLauncher.unregister()
        photoPickerResultLauncher?.unregister()
        galleryResultLauncher?.unregister()
        super.onDestroy()
    }
}