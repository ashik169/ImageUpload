package com.ashik.imageupload.ui

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ashik.imageupload.R
import com.ashik.imageupload.databinding.FragmentUploadImageBinding
import com.ashik.imageupload.utils.FileUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ImageUploadFragment : Fragment() {

    private var _binding: FragmentUploadImageBinding? = null
    private val binding get() = _binding!!

    private var homeCallback: HomeCallback? = null
    private lateinit var callback : OnBackPressedCallback
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

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val photoUri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) requireArguments().getParcelable(
                IMAGE_PATH, Uri::class.java
            ) else requireArguments().getParcelable(IMAGE_PATH)

        Log.d(FileUtils.TAG, photoUri.toString())
        if (photoUri != null) {
            binding.textFileName.text = photoUri.lastPathSegment
            binding.imgPreview.setImageURI(photoUri)
            binding.btnUpload.setOnClickListener(::onClickPreview)
            binding.btnCancel.setOnClickListener(::onClickCancel)
        } else findNavController().popBackStack()
    }

    private fun onClickPreview(@Suppress("UNUSED_PARAMETER") view: View) {
        lifecycleScope.launch {
            homeCallback?.showLoading()
            callback.isEnabled = true
            delay(5000L)
            homeCallback?.hideLoading()
            callback.isEnabled = false
            Toast.makeText(requireContext(),
                getString(R.string.image_upload_success), Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
        }
    }

    private fun onClickCancel(@Suppress("UNUSED_PARAMETER") view: View) {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val IMAGE_PATH = "image_path"
    }
}