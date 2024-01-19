package com.ashik.imageupload.ui.imageoption

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ashik.imageupload.databinding.OptionListItemBinding
import com.ashik.imageupload.model.ImageOption

class ImageOptionAdapter(private val itemCallback: (ImageOption, ImageOptionAdapter.OptionVH, Int) -> Unit) :
    ListAdapter<ImageOption, ImageOptionAdapter.OptionVH>(DIFF_UTIL_CALLBACK) {

    inner class OptionVH(
        private val binding: OptionListItemBinding,
        callback: (OptionVH, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBindData(option: ImageOption) {
            binding.imgMenuIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    itemView.context,
                    option.iconResId
                )
            )
            binding.textTitle.text = itemView.context.resources.getString(option.titleResId)
        }

        init {
            itemView.setOnClickListener {
                adapterPosition.takeIf {
                    it != RecyclerView.NO_POSITION
                }?.let {
                    callback(this@OptionVH, it)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionVH {
        val binding =
            OptionListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OptionVH(binding) { optionVH, i ->
            itemCallback(getItem(i), optionVH, i)
        }
    }

    override fun onBindViewHolder(holder: OptionVH, position: Int) {
        holder.onBindData(getItem(position))
    }

    companion object {
        val DIFF_UTIL_CALLBACK = object : DiffUtil.ItemCallback<ImageOption>() {
            override fun areItemsTheSame(oldItem: ImageOption, newItem: ImageOption): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ImageOption, newItem: ImageOption): Boolean {
                return oldItem == newItem
            }
        }
    }
}
