package com.ashik.imageupload.customview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.core.widget.ImageViewCompat
import com.ashik.imageupload.R
import com.ashik.imageupload.model.FileInfoModel
import com.ashik.imageupload.ui.component.LoadingView
import com.ashik.imageupload.ui.component.PreviewView
import com.ashik.imageupload.utils.ImageCache
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors

class ImageUploadView : FrameLayout {

    companion object {
        const val TAG = "ImageUploadView"
    }


    private lateinit var imgPlaceHolder: ImageView
    private lateinit var labelPlaceHolder: TextView
    private lateinit var chooseButton: Button
    private lateinit var footerButtonLayout: ViewGroup

    private var labelHint: String? = null
    private var hintColor: Int = Color.BLACK
    private var thumbnailPlaceHolder: Drawable? = null
    private var thumbnailTint: Int = Color.BLACK

    private var isPreviewShown: Boolean = false

    private var fileInfo: FileInfoModel? = null

    private var chooseClickListener: OnClickListener? = null
    private var uploadClickListener: ((FileInfoModel) -> Unit)? = null

    private var buttonHeight: Int = 40

    constructor(context: Context) : super(context) {
        initView(context, null, -1)
    }

    constructor(
        context: Context, attrs: AttributeSet?
    ) : super(context, attrs) {
        initView(context, attrs, -1)
    }

    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initView(context, attrs, defStyleAttr)
    }

    private fun initView(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) {
        Log.d(
            TAG, """context -> $context,
            |attrs -> $attrs
            |defStyleAttr -> $defStyleAttr
        """.trimMargin()
        )

        buttonHeight = context.resources.getDimensionPixelSize(R.dimen.button_height)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ImageUploadView)
        labelHint = typedArray.getString(R.styleable.ImageUploadView_android_hint)
        if (isInEditMode) {
            labelHint = labelHint ?: context.getString(R.string.hint_pick_image)
        }
        hintColor = typedArray.getColor(
            R.styleable.ImageUploadView_android_textColorHint,
            ContextCompat.getColor(context, R.color.black)
        )
        thumbnailPlaceHolder = typedArray.getDrawable(
            R.styleable.ImageUploadView_placeholderIcon
        ) ?: AppCompatResources.getDrawable(context, R.drawable.image_filled_24dp)
        thumbnailTint = typedArray.getColor(
            R.styleable.ImageUploadView_placeholderIconTint,
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimary)
        )
        typedArray.recycle()

        // PlaceHolder View
        val placeHolderLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            id = ViewCompat.generateViewId()
            // Image Placeholder
            imgPlaceHolder = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(150, 150)
                gravity = Gravity.CENTER
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageDrawable(thumbnailPlaceHolder)
                ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(thumbnailTint))
            }
            addView(imgPlaceHolder)
            // Label Placeholder
            labelPlaceHolder = TextView(context).apply {
                id = R.id.labelTitle
                setPadding(context.resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin))
                textAlignment = TEXT_ALIGNMENT_CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = labelHint
                setTextColor(hintColor)
            }
            addView(labelPlaceHolder)
            // Choose Button
            chooseButton = MaterialButton(context).apply {
                id = R.id.btnChoose
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, buttonHeight
                ).apply {
                    setMargins(context.resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin))
                }
                text = context.getString(R.string.pick_image)
                setOnClickListener(::onClickChoose)
            }
            addView(chooseButton)
        }
        addView(
            placeHolderLayout,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        )

        // Upload Button
        footerButtonLayout = LinearLayout(context).apply {
            id = ViewCompat.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            isVisible = false
            val cancelButton = MaterialButton(context).apply {
                id = R.id.btnCancel
                text = context.getString(R.string.cancel)
                setBackgroundColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceInverse))
                setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceInverse))
                setOnClickListener { reset() }
            }
            addView(cancelButton, LinearLayout.LayoutParams(0, buttonHeight, 1f).apply {
                marginEnd =
                    (context.resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin) / 2)
            })

            val uploadButton = MaterialButton(context).apply {
                id = R.id.btnUpload
                text = context.getString(R.string.upload_label)
                setOnClickListener { onClickUpload() }
            }
            addView(uploadButton, LinearLayout.LayoutParams(0, buttonHeight, 1f).apply {
                marginEnd =
                    (context.resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin) / 2)
            })
        }
        if(isInEditMode) isVisible = true
        addView(footerButtonLayout, LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.CENTER
        ).apply {
            setMargins(context.resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin))
        })
    }

    fun setOnChooseClickListener(listener: OnClickListener) {
        chooseClickListener = listener
    }

    fun setOnUploadClickListener(listener: (FileInfoModel) -> Unit) {
        uploadClickListener = listener
    }

    private fun onClickChoose(view: View) {
        chooseClickListener?.onClick(view)
    }

    private fun onClickPreview(@Suppress("UNUSED_PARAMETER") view: View) {
        isPreviewShown = true
        var previewLayout = findViewById<View?>(R.id.layoutPreview)
        Log.d(TAG, "previewLayout -> $previewLayout")
        if (previewLayout == null) {
            previewLayout = PreviewView(context).apply {
                id = R.id.layoutPreview
                setImageUri(fileInfo!!.uri)
                setOnCloseClickListener {
                    closePreview()
                }
            }
            addView(
                previewLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            )
        }
    }

    private fun onClickUpload() {
        fileInfo?.let { info ->
            uploadClickListener?.invoke(info)
        }
    }

    fun setFileInfo(info: FileInfoModel?) {
        Log.d(TAG, "info -> $info")
        fileInfo = info
        if (fileInfo != null) {
            labelPlaceHolder.text = info?.fileName
            val file = info!!.file
            val imageCache = ImageCache.getInstance()
            imgPlaceHolder.apply {
                ImageViewCompat.setImageTintList(this, null)
                scaleType = ImageView.ScaleType.CENTER_CROP
                when (val bitmap = imageCache.get(file.absolutePath)) {
                    null -> setImageURI(file.toUri())
                    else -> setImageBitmap(bitmap)
                }
            }
            chooseButton.apply {
                text = context.getString(R.string.label_preview)
                setOnClickListener(::onClickPreview)
            }
            footerButtonLayout.isVisible = true
        } else {
            resetView()
        }
    }

    fun reset() {
        Log.d(TAG, "reset")
        fileInfo = null
        resetView()
    }

    private fun resetView() {
        isPreviewShown = false
        labelPlaceHolder.text = labelHint
        imgPlaceHolder.apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageDrawable(thumbnailPlaceHolder)
            ImageViewCompat.setImageTintList(
                this, ColorStateList.valueOf(thumbnailTint)
            )
        }
        chooseButton.apply {
            text = context.getString(R.string.pick_image)
            setOnClickListener(::onClickChoose)
        }
        footerButtonLayout.isVisible = false
    }

    private fun closePreview() {
        findViewById<View?>(R.id.layoutPreview)?.let {
            this@ImageUploadView.removeView(it)
        }
        isPreviewShown = false
    }

    fun showLoading() {
        var loadingLayout = findViewById<View?>(R.id.layoutLoading)
        Log.d(TAG, "showLoading -> $loadingLayout")
        if (loadingLayout == null) {
            loadingLayout = LoadingView(context).apply {
                id = R.id.layoutLoading
            }
            addView(
                loadingLayout, LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    fun hideLoading() {
        findViewById<View?>(R.id.layoutLoading)?.let {
            this@ImageUploadView.removeView(it)
        }
    }

    private fun isLoading(): Boolean {
        return findViewById<View?>(R.id.layoutLoading) != null
    }

    fun onBackPressed(): Boolean {
        if (isLoading()) return true
        if (isPreviewShown) {
            closePreview()
            return true
        }
        if (fileInfo != null) {
            reset()
            return true
        }
        return false
    }
}