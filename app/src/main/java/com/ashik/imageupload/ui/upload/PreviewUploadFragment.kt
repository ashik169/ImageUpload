package com.ashik.imageupload.ui.upload

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.ashik.imageupload.R
import com.ashik.imageupload.databinding.FragmentPreviewUploadBinding
import com.ashik.imageupload.extensions.showToast
import com.ashik.imageupload.model.ResultState
import com.ashik.imageupload.service.FileBackgroundService
import com.ashik.imageupload.ui.home.HomeCallback
import com.ashik.imageupload.ui.preview.ImagePreviewPagerAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class PreviewUploadFragment : Fragment() {

    private val viewModel: PreviewUploadViewModel by viewModels()

    private var _binding: FragmentPreviewUploadBinding? = null
    private val binding get() = _binding!!

    private var homeCallback: HomeCallback? = null

    private lateinit var previewPagerAdapter: ImagePreviewPagerAdapter
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is HomeCallback) homeCallback = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        previewPagerAdapter = ImagePreviewPagerAdapter(this)
        binding.vpPreview.adapter = previewPagerAdapter
        binding.vpPreview.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val subtitle = "%d/%d \u25CF %s".format(
                    (position + 1),
                    previewPagerAdapter.itemCount,
                    previewPagerAdapter.list[position].fileName
                )
                homeCallback?.updateSubtitle(subtitle)
            }
        })
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uriStateFlow.collectLatest { resultState ->

                    when (resultState) {
                        is ResultState.Error -> {
                            homeCallback?.hideLoading()
                            Log.e(TAG, resultState.exception.message.toString())
                            findNavController().popBackStack()
                        }

                        is ResultState.Loading -> homeCallback?.showLoading()

                        is ResultState.Success -> {
                            homeCallback?.hideLoading()
                            val photoUris = resultState.data
                            if (photoUris.isNotEmpty()) {
                                previewPagerAdapter.addImages(photoUris)
                                binding.btnUpload.setOnClickListener(::onClickPreview)
                                binding.btnCancel.setOnClickListener(::onClickCancel)
                                binding.footerButtons.isVisible = true
                            } else {
                                requireContext().showToast(getString(R.string.images_path_not_found))
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onClickPreview(view: View) {
        view.isEnabled = false
        FileBackgroundService.startUploadService(
            view.context, bundleOf(
                FileBackgroundService.IMAGE_URIS to previewPagerAdapter.list
            )
        )
        view.context.showToast(getString(R.string.msg_img_uploading_background))
        findNavController().popBackStack()
    }

    private fun onClickCancel(view: View) {
        view.isEnabled = false
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "PreviewFragment"

        const val IMAGE_URIS = "image_uris"
    }
}