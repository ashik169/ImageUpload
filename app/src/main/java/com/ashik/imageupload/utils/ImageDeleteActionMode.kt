package com.ashik.imageupload.utils

import android.content.Context
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ashik.imageupload.R

class ImageDeleteActionMode(
    private val context: Context,
    private val onDeleteClicked: () -> Unit,
    private val onDestroyMode: () -> Unit
) :
    ActionMode.Callback {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        val inflater = mode.menuInflater
        inflater.inflate(R.menu.menu_delete, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            onDeleteClicked()
            return true
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        onDestroyMode()
    }
}