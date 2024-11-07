/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import coil3.load
import coil3.request.ImageRequest
import org.lineageos.twelve.R

/**
 * Simple list item view to be used with horizontal RecyclerView adapters.
 */
class HorizontalListItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val thumbnailImageView by lazy { findViewById<ImageView>(R.id.thumbnailImageView) }
    private val headlineTextView by lazy { findViewById<TextView>(R.id.headlineTextView) }
    private val supportingTextView by lazy { findViewById<TextView>(R.id.supportingTextView) }
    private val tertiaryTextView by lazy { findViewById<TextView>(R.id.tertiaryTextView) }

    var thumbnailImage: Drawable?
        get() = thumbnailImageView.drawable
        set(value) {
            thumbnailImageView.setImageDrawable(value)
        }

    var headlineText: CharSequence?
        get() = headlineTextView.text
        set(value) {
            headlineTextView.setTextAndUpdateVisibility(value)
        }

    var headlineMaxLines: Int
        get() = headlineTextView.maxLines
        set(value) {
            headlineTextView.maxLines = value
        }

    var supportingText: CharSequence?
        get() = supportingTextView.text
        set(value) {
            supportingTextView.setTextAndUpdateVisibility(value)
        }

    var supportingMaxLines: Int
        get() = supportingTextView.maxLines
        set(value) {
            supportingTextView.maxLines = value
        }

    var tertiaryText: CharSequence?
        get() = tertiaryTextView.text
        set(value) {
            tertiaryTextView.setTextAndUpdateVisibility(value)
        }

    var tertiaryMaxLines: Int
        get() = tertiaryTextView.maxLines
        set(value) {
            tertiaryTextView.maxLines = value
        }

    init {
        inflate(context, R.layout.horizontal_list_item, this)
    }

    fun loadThumbnailImage(
        data: Any?, builder: ImageRequest.Builder.() -> Unit = {}
    ) = thumbnailImageView.load(data, builder = builder)

    fun setThumbnailImage(bm: Bitmap) = thumbnailImageView.setImageBitmap(bm)
    fun setThumbnailImage(icon: Icon) = thumbnailImageView.setImageIcon(icon)
    fun setThumbnailImage(@DrawableRes resId: Int) = thumbnailImageView.setImageResource(resId)

    fun setHeadlineText(@StringRes resId: Int) = headlineTextView.setTextAndUpdateVisibility(resId)
    fun setHeadlineText(@StringRes resId: Int, vararg formatArgs: Any) =
        headlineTextView.setTextAndUpdateVisibility(resId, *formatArgs)

    fun setSupportingText(@StringRes resId: Int) =
        supportingTextView.setTextAndUpdateVisibility(resId)

    fun setSupportingText(@StringRes resId: Int, vararg formatArgs: Any) =
        supportingTextView.setTextAndUpdateVisibility(resId, *formatArgs)

    fun setTertiaryText(@StringRes resId: Int) = tertiaryTextView.setTextAndUpdateVisibility(resId)
    fun setTertiaryText(@StringRes resId: Int, vararg formatArgs: Any) =
        tertiaryTextView.setTextAndUpdateVisibility(resId, *formatArgs)

    // TextView utils

    private fun TextView.setTextAndUpdateVisibility(text: CharSequence?) {
        this.text = text.also {
            isVisible = it != null
        }
    }

    private fun TextView.setTextAndUpdateVisibility(@StringRes resId: Int) =
        setTextAndUpdateVisibility(resources.getText(resId))

    private fun TextView.setTextAndUpdateVisibility(@StringRes resId: Int, vararg formatArgs: Any) =
        setTextAndUpdateVisibility(resources.getString(resId, *formatArgs))
}
