package com.ashik.imageupload.ui.component

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import com.ashik.imageupload.R
import com.google.android.material.color.MaterialColors

class LoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {


    private val loadingText: TextView
    private val progressBar: ProgressBar

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setBackgroundColor(MaterialColors.compositeARGBWithAlpha(Color.WHITE, 100))
        progressBar = ProgressBar(context).apply {
            id = ViewCompat.generateViewId()
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
            )
            isIndeterminate = true
        }
        addView(progressBar)

        loadingText = TextView(context).apply {
            id = ViewCompat.generateViewId()
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
            )
            text = context.getString(R.string.uploading_images)
            setPadding(context.resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin))
        }
        addView(loadingText)
    }
}