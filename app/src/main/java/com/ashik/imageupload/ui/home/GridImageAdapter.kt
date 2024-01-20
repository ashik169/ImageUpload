package com.ashik.imageupload.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ashik.imageupload.databinding.LayoutGridImageBinding
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.utils.ImageCache

class GridImageAdapter(private val itemCallback: (FileInfoModel, Int) -> Unit) :
    ListAdapter<FileInfoModel, GridImageAdapter.GridImageVH>(DIFF_UTIL_CALLBACK) {

    val imageCache = ImageCache.getInstance()

    inner class GridImageVH(
        private val binding: LayoutGridImageBinding, callback: (GridImageVH, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBindData(fileInfoModel: FileInfoModel) {
            when (val bitmap = imageCache.get(fileInfoModel.file.absolutePath)) {
                null -> binding.imagePreview.setImageURI(fileInfoModel.file.toUri())
                else -> binding.imagePreview.setImageBitmap(bitmap)
            }
        }

        init {
            itemView.setOnClickListener {
                adapterPosition.takeIf {
                    it != RecyclerView.NO_POSITION
                }?.let {
                    callback(this@GridImageVH, it)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridImageVH {
        val binding =
            LayoutGridImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GridImageVH(binding) { optionVH, i ->
            itemCallback(getItem(i), i)
        }
    }

    override fun onBindViewHolder(holder: GridImageAdapter.GridImageVH, position: Int) {
        holder.onBindData(getItem(position))
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