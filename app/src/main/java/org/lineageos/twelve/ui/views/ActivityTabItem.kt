/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.ImageRequest
import com.google.android.material.carousel.MaskableFrameLayout
import org.lineageos.twelve.R
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.MediaItem
import org.lineageos.twelve.models.Playlist
import kotlin.math.pow

class ActivityTabItem(view: View) : RecyclerView.ViewHolder(view) {
    private val rootView by lazy {
        itemView.findViewById<MaskableFrameLayout>(R.id.activityItemRoot)
    }
    private val headlineTextView by lazy {
        itemView.findViewById<TextView>(R.id.headlineTextView)
    }
    private val placeholderImageView by lazy {
        itemView.findViewById<ImageView>(R.id.placeholderImageView)
    }
    private val thumbnailImageView by lazy {
        itemView.findViewById<ImageView>(R.id.thumbnailImageView)
    }

    private var headlineText: CharSequence?
        get() = headlineTextView.text
        set(value) {
            headlineTextView.text = value
        }

    init {
        rootView.setOnMaskChangedListener { rect ->
            val progress = (rect.width() / rootView.width).pow(2)
            headlineTextView.alpha = progress
        }
    }

    fun setItem(item: MediaItem<*>) {
        when (item) {
            is Album -> {
                item.title?.let {
                    headlineText = it
                } ?: setHeadlineText(R.string.album_unknown)
                loadThumbnailImage(item.thumbnail, R.drawable.ic_album)
            }

            is Artist -> {
                item.name?.let {
                    headlineText = it
                } ?: setHeadlineText(R.string.artist_unknown)
                loadThumbnailImage(item.thumbnail, R.drawable.ic_person)
            }

            is Audio -> {
                headlineText = item.title
                loadThumbnailImage(item.thumbnail, R.drawable.ic_music_note)
            }

            is Genre -> {
                item.name?.let {
                    headlineText = it
                } ?: setHeadlineText(R.string.genre_unknown)
                onNoThumbnail(R.drawable.ic_genres)
            }

            is Playlist -> {
                headlineText = item.name
                onNoThumbnail(R.drawable.ic_playlist_play)
            }
        }
    }

    private fun loadThumbnailImage(
        data: Any?,
        @DrawableRes placeholder: Int? = null,
        builder: ImageRequest.Builder.() -> Unit = {
            listener(
                onCancel = { onNoThumbnail(placeholder) },
                onError = { _, _ -> onNoThumbnail(placeholder) },
                onSuccess = { _, _ ->
                    placeholderImageView.isVisible = false
                    headlineTextView.apply {
                        setBackgroundResource(R.drawable.bg_dark_fade)
                        setTextColor(context.getColor(android.R.color.white))
                    }
                    thumbnailImageView.isVisible = true
                },
            )
        }
    ) = thumbnailImageView.load(data, builder = builder)

    private fun setHeadlineText(@StringRes resId: Int) =
        headlineTextView.setText(resId)

    private fun onNoThumbnail(@DrawableRes placeholder: Int? = null) {
        placeholder?.let {
            placeholderImageView.setImageResource(it)
            placeholderImageView.isVisible = true
        }

        thumbnailImageView.isVisible = false
        headlineTextView.apply {
            val tv = TypedValue()
            context.theme.resolveAttribute(
                com.google.android.material.R.attr.colorOnPrimary,
                tv,
                true,
            )
            setTextColor(tv.data)
            setBackgroundResource(0)
        }
    }
}
