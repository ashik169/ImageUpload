package com.ashik.imageupload.ui.home

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ashik.imageupload.R
import com.ashik.imageupload.databinding.FragmentHomeBinding
import com.ashik.imageupload.databinding.PickImageOptionsBinding
import com.ashik.imageupload.model.ImageOption
import com.ashik.imageupload.model.ResultState
import com.ashik.imageupload.service.ImageUploadService
import com.ashik.imageupload.ui.IMediaResultCallback
import com.ashik.imageupload.ui.MediaResultContract
import com.ashik.imageupload.ui.imageoption.ImageOptionAdapter
import com.ashik.imageupload.ui.preview.GalleryFragment
import com.ashik.imageupload.ui.upload.PreviewUploadFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class HomeFragment : Fragment(), IMediaResultCallback {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var mediaResultContract: MediaResultContract

    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaResultContract =
            MediaResultContract(requireContext(), requireActivity().activityResultRegistry, this)
        lifecycle.addObserver(mediaResultContract)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ImageUploadService.UPLOAD_PROGRESS.observe(viewLifecycleOwner) {
            when {
                it == null -> {
                    // Do Nothing
                }

                it.isDone -> {
                    viewModel.fetchImages()
                    binding.layoutUploadStatus.isVisible = true
                    binding.textUploadStatus.text = getString(R.string.image_upload_success)
                    binding.textUploadProgress.isVisible = false
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(2000)
                        binding.layoutUploadStatus.isVisible = false
                    }
                }

                else -> {
                    binding.layoutUploadStatus.isVisible = true
                    binding.textUploadStatus.text = "%s %d/%d".format(
                        getString(R.string.label_uploading),
                        it.fileIndex,
                        it.totalFile
                    )
                    binding.textUploadProgress.text = "%d%s".format(it.progress, "%")
                }
            }
        }
        binding.swipeRefresh.setOnRefreshListener(viewModel::fetchImages)
        val gridImageAdapter = GridImageAdapter { _, index ->
            findNavController().navigate(
                R.id.action_navigate_to_preview, bundleOf(
                    GalleryFragment.FILE_INDEX to index
                )
            )
        }
        binding.fabPickImages.setOnClickListener(::onClickPickImage)
        binding.rvImages.apply {
            layoutManager = GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
            adapter = gridImageAdapter
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.files.collectLatest {
                    binding.swipeRefresh.isRefreshing = it is ResultState.Loading
                    when (it) {
                        is ResultState.Error -> {
                            binding.layoutPlaceholder.isVisible = true
                            gridImageAdapter.submitList(listOf())
                        }

                        is ResultState.Loading -> {
                            binding.layoutPlaceholder.isVisible = true
                            gridImageAdapter.submitList(listOf())
                        }

                        is ResultState.Success -> {
                            val files = it.data
                            binding.layoutPlaceholder.isVisible = files.isEmpty()
                            gridImageAdapter.submitList(files)
                        }
                    }
                }
            }
        }
    }

    private fun onClickPickImage(view: View) {
        val imageOptions = ImageOption.IMAGE_OPTIONS
        val bottomSheetDialog = BottomSheetDialog(view.context)
        val pickImageOptionsBinding = PickImageOptionsBinding.inflate(LayoutInflater.from(context))
        bottomSheetDialog.setContentView(pickImageOptionsBinding.root)
        val imageOptionAdapter = ImageOptionAdapter { option, _, _ ->
            when (option.id) {
                ImageOption.CAMERA -> mediaResultContract.openCamera()
                else -> mediaResultContract.openGallery()
            }
            bottomSheetDialog.dismiss()
        }
        imageOptionAdapter.submitList(imageOptions)
        pickImageOptionsBinding.rvOptions.apply {
//            layoutManager = LinearLayoutManager(context)
            layoutManager = GridLayoutManager(context, 2)
            adapter = imageOptionAdapter
        }
        bottomSheetDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPermissionDenied(): Boolean {
        return false
    }

    override fun onImageResult(uris: List<Uri>) {
        findNavController().navigate(
            R.id.action_navigate_to_upload_image, bundleOf(
                PreviewUploadFragment.IMAGE_URIS to uris
            )
        )
    }
}