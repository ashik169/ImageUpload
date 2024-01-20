package com.ashik.imageupload.ui.preview

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.ashik.imageupload.databinding.LayoutImagePreviewBinding
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.utils.ImageCache

class ImagePreviewFragment : Fragment() {

    companion object {
        private const val IMAGE_URI = "image_uri"
        fun newInstance(uri: FileInfoModel): ImagePreviewFragment {
            val args = Bundle().apply {
                putParcelable(IMAGE_URI, uri)
            }

            val fragment = ImagePreviewFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val imageCache = ImageCache.getInstance()

    private var _binding: LayoutImagePreviewBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = LayoutImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fileInfoModel =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) requireArguments().getParcelable(
                IMAGE_URI, FileInfoModel::class.java
            ) else requireArguments().getParcelable(IMAGE_URI)
        if (fileInfoModel != null) {
            when (val bitmap = imageCache.get(fileInfoModel.file.absolutePath)) {
                null -> binding.imagePreview.setImageURI(fileInfoModel.file.toUri())
                else -> binding.imagePreview.setImageBitmap(bitmap)
            }
        }
    }
}