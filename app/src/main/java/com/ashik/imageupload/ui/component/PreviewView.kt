package com.ashik.imageupload.ui.component

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import androidx.core.widget.ImageViewCompat
import com.ashik.imageupload.R

class PreviewView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var closeIcon: ImageView
    private var imageView: ImageView

    init {
        imageView = ImageView(context).apply {
            id = ViewCompat.generateViewId()
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.BLACK)
        }
        addView(imageView)

        closeIcon = ImageView(context).apply {
            id = ViewCompat.generateViewId()
            layoutParams = LayoutParams(140, 140, Gravity.TOP or Gravity.END)
            setPadding(context.resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin))
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cancel_24dp))
            ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(Color.WHITE))
        }
        addView(closeIcon)
    }

    fun setOnCloseClickListener(listener: OnClickListener) {
        closeIcon.setOnClickListener(listener)
    }

    fun setImageUri(uri: Uri?) {
        imageView.setImageURI(uri)
    }
}