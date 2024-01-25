package com.ashik.imageupload.ui.component

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ashik.imageupload.databinding.FragmentImageUploadViewBinding
import com.ashik.imageupload.extensions.showChooseOptions
import com.ashik.imageupload.model.ImageOption
import com.ashik.imageupload.service.FileBackgroundService
import com.ashik.imageupload.ui.IMediaResultCallback
import com.ashik.imageupload.ui.MediaResultContract
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ImageUploadViewFragment : Fragment(), IMediaResultCallback {

    private var _binding: FragmentImageUploadViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaResultContract: MediaResultContract

    private val viewModel by activityViewModels<ImageUploadViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val imageUploadView = binding.root
                if (!imageUploadView.onBackPressed()) {
                    viewModel.clearImage()
                    if (!findNavController().popBackStack()) {
                        activity?.finish()
                    }
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaResultContract =
            MediaResultContract(requireContext(), requireActivity().activityResultRegistry, this)
        lifecycle.addObserver(mediaResultContract)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageUploadViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FileBackgroundService.FILE_BACKGROUND_PROGRESS.observe(viewLifecycleOwner) {
            when {
                it == null -> {
                    binding.root.hideLoading()
                }

                it.isDone -> {
                    binding.root.reset()
                    binding.root.hideLoading()
                }

                else -> {
                    binding.root.showLoading()
                }
            }
        }
        binding.root.setOnChooseClickListener {
            it.context.showChooseOptions { option ->
                when (option.id) {
                    ImageOption.CAMERA -> mediaResultContract.openCamera()
                    else -> mediaResultContract.openGallery()
                }
            }
        }

        binding.root.setOnUploadClickListener {
            FileBackgroundService.startUploadService(
                view.context, bundleOf(
                    FileBackgroundService.IMAGE_URIS to mutableListOf(it)
                )
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.fileInfo.collectLatest {
                    Log.d(ImageUploadViewModel.TAG, "fileInfo -> $it")
                    binding.root.setFileInfo(it)
                }
            }
        }
    }

    override fun onPermissionDenied() = false

    override fun onImageResult(uris: List<Uri>) {
        Log.d("ImageUpload", "onImageResult")
        viewModel.updateImage(uris)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}