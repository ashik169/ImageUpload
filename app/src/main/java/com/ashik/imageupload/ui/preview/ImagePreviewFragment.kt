package com.ashik.imageupload.ui.preview

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ashik.imageupload.databinding.LayoutImagePreviewBinding

class ImagePreviewFragment : Fragment() {

    companion object {
        private const val IMAGE_URI = "image_uri"
        fun newInstance(uri: Uri): ImagePreviewFragment {
            val args = Bundle().apply {
                putParcelable(IMAGE_URI, uri)
            }

            val fragment = ImagePreviewFragment()
            fragment.arguments = args
            return fragment
        }
    }


    private var _binding: LayoutImagePreviewBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = LayoutImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val photoUri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) requireArguments().getParcelable(
                IMAGE_URI, Uri::class.java
            ) else requireArguments().getParcelable(IMAGE_URI)
        binding.imagePreview.setImageURI(photoUri)
    }
}