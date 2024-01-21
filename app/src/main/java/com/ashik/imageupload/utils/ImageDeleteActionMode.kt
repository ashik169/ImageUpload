package com.ashik.imageupload.utils

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import com.ashik.imageupload.R

class ImageDeleteActionMode(
    private val onShareClicked: () -> Unit,
    private val onDeleteClicked: () -> Unit,
    private val onDestroyMode: () -> Unit
) :
    ActionMode.Callback {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        val inflater = mode.menuInflater
        inflater.inflate(R.menu.menu_grid_action_mode, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                onDeleteClicked()
                return true
            }
            R.id.action_share -> {
                onShareClicked()
                return true
            }
            else -> return false
        }
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        onDestroyMode()
    }
}