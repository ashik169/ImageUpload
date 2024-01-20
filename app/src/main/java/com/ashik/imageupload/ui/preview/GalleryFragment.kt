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
import com.ashik.imageupload.model.ResultState
import com.ashik.imageupload.ui.home.HomeCallback
import com.ashik.imageupload.ui.home.HomeViewModel
import com.ashik.imageupload.ui.preview.info.FileInfoDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class GalleryFragment : Fragment(), MenuProvider {

    private val viewModel: HomeViewModel by activityViewModels()

    private var _binding: FragmentGalleryBinding? = null
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
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val fileIndex = requireArguments().getInt(FILE_INDEX, 0)

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
                viewModel.files.collectLatest { resultState ->
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
                                binding.vpPreview.setCurrentItem(fileIndex, false)
                                binding.vpPreview.isVisible = true
                                binding.textPageIndicator.isVisible = photoUris.size > 1
                            } else {
                                findNavController().popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onPageChanged(position: Int) {
        homeCallback?.updateSubtitle(previewPagerAdapter.list[position].fileName)
        binding.textPageIndicator.text =
            "%d/%d".format((position + 1), previewPagerAdapter.itemCount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "PreviewFragment"

        const val FILE_INDEX = "file_index"
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_info, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.item_info) {
            findNavController().navigate(
                R.id.action_navigate_to_file_info, bundleOf(
                    FileInfoDialogFragment.FILE_INFO to previewPagerAdapter.list[binding.vpPreview.currentItem]
                )
            )
        }
        return true
    }
}