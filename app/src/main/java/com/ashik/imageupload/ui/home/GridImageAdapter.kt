package com.ashik.imageupload.ui.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ashik.imageupload.R
import com.ashik.imageupload.databinding.LayoutGridImageBinding
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.utils.ImageCache

class GridImageAdapter(
    private val onItemClicked: (GridImageAdapter.GridImageVH, FileInfoModel, Int) -> Unit,
    private val onItemLongClicked: (GridImageAdapter.GridImageVH, FileInfoModel, Int) -> Unit,
    private val onSelectionChanged: () -> Unit,
) : ListAdapter<FileInfoModel, GridImageAdapter.GridImageVH>(DIFF_UTIL_CALLBACK) {

    val imageCache = ImageCache.getInstance()

    var multiSelect: Boolean = false
    val selectedItems = mutableListOf<FileInfoModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridImageVH {
        val binding =
            LayoutGridImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GridImageVH(binding = binding, itemClicked = { imageVH, i ->
            if (multiSelect) {
                selectItem(imageVH, getItem(i))
                onSelectionChanged()
            } else onItemClicked(imageVH, getItem(i), i)
        }, itemLongClicked = { imageVH, i ->
            if (!multiSelect) {
                multiSelect = true
                selectItem(imageVH, getItem(i))
                onItemLongClicked(imageVH, getItem(i), i)
                onSelectionChanged()
            }
        })
    }

    private fun selectItem(holder: GridImageAdapter.GridImageVH, infoModel: FileInfoModel) {
        // If the "selectedItems" list contains the item, remove it and set it's state to normal
        if (selectedItems.contains(infoModel)) {
            infoModel.isSelected = false
            selectedItems.remove(infoModel)
            holder.itemView.findViewById<ImageView>(R.id.imagePreview).alpha = 1.0f
        } else {
            // Else, add it to the list and add a darker shade over the image, letting the user know that it was selected
            selectedItems.add(infoModel)
            infoModel.isSelected = true
            holder.itemView.findViewById<ImageView>(R.id.imagePreview).alpha = 0.3f
        }
    }

    override fun onBindViewHolder(holder: GridImageAdapter.GridImageVH, position: Int) {
        holder.onBindData(getItem(position))
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetSelection() {
        multiSelect = false
        selectedItems.clear()
        currentList.onEach { it.isSelected = false }
//        submitList(list)
        notifyItemRangeChanged(0, itemCount)
    }

    inner class GridImageVH(
        private val binding: LayoutGridImageBinding,
        itemClicked: (GridImageVH, Int) -> Unit,
        itemLongClicked: (GridImageVH, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBindData(fileInfoModel: FileInfoModel) {
            val file = fileInfoModel.file
            binding.imagePreview.alpha = if (fileInfoModel.isSelected) 0.3f else 1.0f
            when (val bitmap = imageCache.get("${file.absolutePath}_thumb")) {
                null -> binding.imagePreview.setImageURI(file.toUri())
                else -> binding.imagePreview.setImageBitmap(bitmap)
            }
        }

        init {
            itemView.setOnClickListener {
                adapterPosition.takeIf {
                    it != RecyclerView.NO_POSITION
                }?.let {
                    itemClicked(this@GridImageVH, it)
                }
            }

            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return@setOnLongClickListener false
                }
                itemLongClicked(this@GridImageVH, position)
                return@setOnLongClickListener true
            }
        }
    }

    companion object {
        val DIFF_UTIL_CALLBACK = object : DiffUtil.ItemCallback<FileInfoModel>() {
            override fun areItemsTheSame(oldItem: FileInfoModel, newItem: FileInfoModel): Boolean {
                return oldItem.file.absolutePath == newItem.file.absolutePath
            }

            override fun areContentsTheSame(
                oldItem: FileInfoModel, newItem: FileInfoModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}