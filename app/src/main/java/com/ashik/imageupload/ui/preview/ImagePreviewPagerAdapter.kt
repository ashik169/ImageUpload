package com.ashik.imageupload.ui.preview

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ashik.imageupload.model.FileInfoModel

class ImagePreviewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    val list = mutableListOf<FileInfoModel>()

    @SuppressLint("NotifyDataSetChanged")
    fun addImages(imageUris: List<FileInfoModel>) {
        list.clear()
        list.addAll(imageUris)
        notifyDataSetChanged()
    }

    override fun getItemCount() = list.size

    override fun createFragment(position: Int): Fragment {
        return ImagePreviewFragment.newInstance(list[position])
    }
}