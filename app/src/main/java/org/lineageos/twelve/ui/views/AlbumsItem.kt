/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.core.view.isVisible
import coil3.load
import coil3.request.ImageRequest
import com.google.android.material.card.MaterialCardView
import org.lineageos.twelve.R
import org.lineageos.twelve.models.Album

class AlbumsItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle,
) : MaterialCardView(context, attrs, defStyleAttr) {
    private val albumNameTextView by lazy { findViewById<TextView>(R.id.headlineTextView) }
    private val placeholderImageView by lazy { findViewById<ImageView>(R.id.placeholderImageView) }
    private val artistNameTextView by lazy { findViewById<TextView>(R.id.subheadTextView) }
    private val thumbnailImageView by lazy { findViewById<ImageView>(R.id.thumbnailImageView) }

    private var albumNameText: CharSequence?
        get() = albumNameTextView.text
        set(value) {
            albumNameTextView.text = value ?: context.getString(R.string.album_unknown)
        }

    private var artistNameText: CharSequence?
        get() = artistNameTextView.text
        set(value) {
            artistNameTextView.text = value ?: context.getString(R.string.artist_unknown)
        }

    init {
        setCardBackgroundColor(Color.TRANSPARENT)
        cardElevation = 0f
        strokeWidth = 0

        inflate(context, R.layout.item_album, this)
    }

    fun setItem(item: Album) {
        albumNameText = item.title
        artistNameText = item.artistName

        // Defer loading the thumbnail so that we have a valid size
        loadThumbnailImage(item.thumbnail)
    }

    private fun showPlaceHolderImage() {
        placeholderImageView.isVisible = true
        thumbnailImageView.isVisible = false
    }

    private fun loadThumbnailImage(
        data: Any?,
        builder: ImageRequest.Builder.() -> Unit = {
            listener(
                onCancel = {
                    showPlaceHolderImage()
                },
                onError = { _, _ ->
                    showPlaceHolderImage()
                },
                onSuccess = { _, _ ->
                    placeholderImageView.isVisible = false
                    thumbnailImageView.isVisible = true
                },
            )
        }
    ) = thumbnailImageView.load(data, builder = builder)
}
