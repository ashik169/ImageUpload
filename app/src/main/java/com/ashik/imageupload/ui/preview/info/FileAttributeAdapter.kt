package com.ashik.imageupload.ui.preview.info

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ashik.imageupload.databinding.FileInfoListItemBinding
import com.ashik.imageupload.model.FileAttribute

class FileAttributeAdapter :
    ListAdapter<FileAttribute, FileAttributeAdapter.FileAttributeVH>(DIFF_UTIL_CALLBACK) {

    inner class FileAttributeVH(
        private val binding: FileInfoListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBindData(option: FileAttribute) {
            binding.textTitle.text = option.label
            binding.textSubTitle.text = option.value
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileAttributeVH {
        val binding =
            FileInfoListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileAttributeVH(binding)
    }

    override fun onBindViewHolder(holder: FileAttributeVH, position: Int) {
        holder.onBindData(getItem(position))
    }

    companion object {
        val DIFF_UTIL_CALLBACK = object : DiffUtil.ItemCallback<FileAttribute>() {
            override fun areItemsTheSame(oldItem: FileAttribute, newItem: FileAttribute): Boolean {
                return oldItem.label == newItem.label
            }

            override fun areContentsTheSame(
                oldItem: FileAttribute, newItem: FileAttribute
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}