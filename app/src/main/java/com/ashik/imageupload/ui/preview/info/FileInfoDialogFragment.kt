package com.ashik.imageupload.ui.preview.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ashik.imageupload.databinding.DialogFileInfoBinding
import com.ashik.imageupload.extensions.showToast
import com.ashik.imageupload.model.ResultState
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FileInfoDialogFragment : BottomSheetDialogFragment() {

    companion object {
//        const val TAG = "FileInfoDialog"

        const val FILE_INFO = "file_info"
    }

    private val fileInfoViewModel: FileInfoViewModel by viewModels()

    private var _binding: DialogFileInfoBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogFileInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fileAttributeAdapter = FileAttributeAdapter()
        binding.rvFileInfo.adapter = fileAttributeAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                fileInfoViewModel.fileInfo.collectLatest {
                    binding.progressBar.isVisible = it is ResultState.Loading
                    when (it) {
                        is ResultState.Error -> {
                            requireContext().showToast(it.exception.message.toString())
                            findNavController().popBackStack()
                        }

                        is ResultState.Loading -> {
                            // Do Nothing
                        }

                        is ResultState.Success -> {
                            fileAttributeAdapter.submitList(it.data)
                        }
                    }
                }
            }
        }
    }
}