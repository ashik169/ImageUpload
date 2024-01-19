package com.ashik.imageupload.ui.preview

import android.annotation.SuppressLint
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ImagePreviewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    val list = mutableListOf<Uri>()

    @SuppressLint("NotifyDataSetChanged")
    fun addImages(imageUris: List<Uri>) {
        list.clear()
        list.addAll(imageUris)
        notifyDataSetChanged()
    }

    override fun getItemCount() = list.size

    override fun createFragment(position: Int): Fragment {
        return ImagePreviewFragment.newInstance(list[position])
    }
}