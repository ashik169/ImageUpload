package com.ashik.imageupload.ui.preview

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.ashik.imageupload.R
import com.ashik.imageupload.databinding.FragmentGalleryBinding
import com.ashik.imageupload.extensions.showToast
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.model.ResultState
import com.ashik.imageupload.ui.home.HomeCallback
import com.ashik.imageupload.ui.home.HomeViewModel
import com.ashik.imageupload.ui.preview.info.FileInfoDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class GalleryFragment : Fragment(), MenuProvider {

    companion object {
        const val TAG = "PreviewFragment"

        const val FILE_INDEX = "file_index"
    }


    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    private var homeCallback: HomeCallback? = null

    private lateinit var previewPagerAdapter: ImagePreviewPagerAdapter

    private var itemRemoved: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is HomeCallback) homeCallback = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fileIndex = requireArguments().getInt(FILE_INDEX, 0)

//        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.btnInfo.setOnClickListener { onClickInfo() }
        binding.btnShare.setOnClickListener { onClickShare() }
        binding.btnDelete.setOnClickListener { onClickDelete() }
        previewPagerAdapter = ImagePreviewPagerAdapter(this)
        binding.vpPreview.apply {
            isVisible = false
            adapter = previewPagerAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    onPageChanged(position)
                }
            })
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    viewModel.files.collectLatest { onFileReceived(it, fileIndex) }
                }
                launch {
                    viewModel.deleteFile.collectLatest {
                        if (it.isSuccess) {
                            val itemCount = previewPagerAdapter.itemCount
                            it.getOrDefault(-1).takeIf { it >= 0 }?.let { pos ->
                                itemRemoved = true
                                if (itemCount == 1) {
                                    // first index
                                    // Do nothing
                                } else if (pos == itemCount - 1) {
                                    // last index
                                    binding.vpPreview.currentItem = pos - 1
                                } else {
                                    binding.vpPreview.currentItem = pos + 1
                                }
                                previewPagerAdapter.removeItem(pos)
                            }
                            if (itemCount == 0) {
                                findNavController().popBackStack()
                            }
                        } else {
                            context?.showToast(
                                it.exceptionOrNull()?.message ?: "Failed to delete image"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun onFileReceived(
        resultState: ResultState<List<FileInfoModel>>, fileIndex: Int
    ) {
        when (resultState) {
            is ResultState.Error -> {
                Log.e(TAG, resultState.exception.message.toString())
                findNavController().popBackStack()
            }

            is ResultState.Loading -> {
                // Do Nothing
            }

            is ResultState.Success -> {
                val photoUris = resultState.data
                if (photoUris.isNotEmpty()) {
                    if (!itemRemoved) {
                        previewPagerAdapter.addImages(photoUris)
                        binding.vpPreview.setCurrentItem(fileIndex, false)
                        onPageChanged(fileIndex)
                        binding.vpPreview.isVisible = true
                        binding.textPageIndicator.isVisible = photoUris.size > 1
                        binding.footerButtons.isVisible = true
                    } else itemRemoved = false
                } else {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun onPageChanged(position: Int) {
        if (previewPagerAdapter.itemCount == 0) return
        homeCallback?.updateSubtitle(previewPagerAdapter.list[position].fileName)
        binding.textPageIndicator.text =
            "%d/%d".format((position + 1), previewPagerAdapter.itemCount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_image_preview, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.item_info) {
            onClickInfo()
        } else if (menuItem.itemId == R.id.item_share) {
            onClickShare()
        }
        return true
    }

    private fun onClickDelete() {
        val currentItem = binding.vpPreview.currentItem
        val fileInfoModel = previewPagerAdapter.list[currentItem]
        MaterialAlertDialogBuilder(requireContext()).setIcon(R.drawable.ic_delete_24dp)
            .setTitle(getString(R.string.title_delete_images))
            .setMessage(getString(R.string.msg_delete_images))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteImage(fileInfoModel, currentItem)
            }.setNegativeButton(getString(R.string.cancel), null).show()
    }

    private fun onClickShare() {
        val fileInfoModel = previewPagerAdapter.list[binding.vpPreview.currentItem]
        homeCallback?.shareFile(fileInfoModel)
    }

    private fun onClickInfo() {
        val fileInfoModel = previewPagerAdapter.list[binding.vpPreview.currentItem]
        findNavController().navigate(
            R.id.action_navigate_to_file_info, bundleOf(
                FileInfoDialogFragment.FILE_INFO to fileInfoModel
            )
        )
    }
}