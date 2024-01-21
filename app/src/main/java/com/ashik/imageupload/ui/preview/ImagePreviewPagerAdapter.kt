package com.ashik.imageupload.ui.preview

import android.annotation.SuppressLint
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ashik.imageupload.model.FileInfoModel

class ImagePreviewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    val list = mutableListOf<FileInfoModel>()

    private lateinit var itemIds: List<Long>

    @SuppressLint("NotifyDataSetChanged")
    fun addImages(imageUris: List<FileInfoModel>) {
        list.clear()
        list.addAll(imageUris)
        updateItemIds()
        notifyDataSetChanged()
    }

    override fun getItemCount() = list.size

    override fun createFragment(position: Int): Fragment {
        return ImagePreviewFragment.newInstance(list[position])
    }

    override fun getItemId(position: Int) = list[position].hashCode().toLong()

    private fun updateItemIds() {
        itemIds = list.map { it.hashCode().toLong() }
    }

    override fun containsItem(itemId: Long): Boolean = itemIds.contains(itemId)

    fun removeItem(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
//        notifyItemRangeChanged(position, itemCount)
        updateItemIds()
    }
}