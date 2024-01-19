package com.ashik.imageupload.ui.preview

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.ashik.imageupload.databinding.FragmentUploadImageBinding
import com.ashik.imageupload.model.ResultState
import com.ashik.imageupload.service.ImageUploadService
import com.ashik.imageupload.ui.home.HomeCallback
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import showToast

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class PreviewFragment : Fragment() {

    private val viewModel: PreviewViewModel by viewModels()

    private var _binding: FragmentUploadImageBinding? = null
    private val binding get() = _binding!!

    private var homeCallback: HomeCallback? = null
    private lateinit var callback: OnBackPressedCallback

    private lateinit var previewPagerAdapter: ImagePreviewPagerAdapter
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is HomeCallback) homeCallback = context
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    val loading = homeCallback?.isLoading
                    if (loading == null || !loading) findNavController().popBackStack()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewPagerAdapter = ImagePreviewPagerAdapter(this)
        binding.vpPreview.adapter = previewPagerAdapter
        binding.vpPreview.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.textFileName.text = previewPagerAdapter.list[position].lastPathSegment
            }
        })
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uriStateFlow.collectLatest { resultState: ResultState<List<Uri>> ->
                    when (resultState) {
                        is ResultState.Error -> {
                            Log.e(TAG, resultState.exception.message.toString())
                            findNavController().popBackStack()
                        }

                        is ResultState.Loading -> Log.i(TAG, "Loading...")
                        is ResultState.Success -> {
                            val photoUris = resultState.data
                            if (photoUris.isNotEmpty()) {
                                previewPagerAdapter.addImages(photoUris)
                                binding.btnUpload.setOnClickListener(::onClickPreview)
                                binding.btnCancel.setOnClickListener(::onClickCancel)
                            } else {
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onClickPreview(view: View) {
        ImageUploadService.startService(
            view.context, bundleOf(
                ImageUploadService.IMAGE_URIS to previewPagerAdapter.list
            )
        )
        view.context.showToast("Images are uploading from background")
        findNavController().popBackStack()/*lifecycleScope.launch {
            homeCallback?.showLoading()
            callback.isEnabled = true
            delay(5000L)
            homeCallback?.hideLoading()
            callback.isEnabled = false
            Toast.makeText(
                requireContext(),
                getString(R.string.image_upload_success), Toast.LENGTH_LONG
            ).show()
            findNavController().popBackStack()
        }*/
    }

    private fun onClickCancel(@Suppress("UNUSED_PARAMETER") view: View) {
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