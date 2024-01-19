package com.ashik.imageupload.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ashik.imageupload.databinding.LayoutGridImageBinding
import java.io.File

class GridImageAdapter(private val itemCallback: (File) -> Unit) :
    ListAdapter<File, GridImageAdapter.GridImageVH>(DIFF_UTIL_CALLBACK) {

    inner class GridImageVH(
        private val binding: LayoutGridImageBinding,
        callback: (GridImageVH, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBindData(file: File) {
            binding.imagePreview.setImageURI(file.toUri())
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
            itemCallback(getItem(i))
        }
    }

    override fun onBindViewHolder(holder: GridImageAdapter.GridImageVH, position: Int) {
        holder.onBindData(getItem(position))
    }

    companion object {
        val DIFF_UTIL_CALLBACK = object : DiffUtil.ItemCallback<File>() {
            override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
                return oldItem == newItem
            }
        }
    }
}