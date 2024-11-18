/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.content.Context
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.lineageos.twelve.R
import org.lineageos.twelve.models.ActivityTab
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.MediaItem
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.areContentsTheSame
import org.lineageos.twelve.models.areItemsTheSame
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter

class ActivityTabItem(context: Context) : FrameLayout(context) {
    // Views
    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.recyclerView) }
    private val titleTextView by lazy { findViewById<TextView>(R.id.titleTextView) }

    // RecyclerView
    private val adapter = object : SimpleListAdapter<MediaItem<*>, HorizontalListItem>(
        mediaItemDiffCallback,
        ::HorizontalListItem,
    ) {
        override fun ViewHolder.onBindView(item: MediaItem<*>) {
            when (item) {
                is Album -> {
                    view.headlineText = item.title
                    view.supportingText = item.artistName
                    view.tertiaryText = item.year?.toString()

                    item.thumbnail?.uri?.let {
                        view.loadThumbnailImage(it)
                    } ?: item.thumbnail?.bitmap?.let {
                        view.loadThumbnailImage(it)
                    } ?: view.setThumbnailImage(R.drawable.ic_album)
                }

                is Artist -> {
                    view.headlineText = item.name
                    view.supportingText = null
                    view.tertiaryText = null

                    item.thumbnail?.uri?.let {
                        view.loadThumbnailImage(it)
                    } ?: item.thumbnail?.bitmap?.let {
                        view.loadThumbnailImage(it)
                    } ?: view.setThumbnailImage(R.drawable.ic_person)
                }

                is Audio -> {
                    view.headlineText = item.title
                    view.supportingText = item.artistName
                    view.tertiaryText = item.albumTitle

                    view.setThumbnailImage(R.drawable.ic_music_note)
                }

                is Genre -> {
                    view.headlineText = item.name
                    view.supportingText = null
                    view.tertiaryText = null

                    view.setThumbnailImage(R.drawable.ic_genres)
                }

                is Playlist -> {
                    view.headlineText = item.name
                    view.supportingText = null
                    view.tertiaryText = null

                    view.setThumbnailImage(R.drawable.ic_playlist_play)
                }
            }
        }
    }

    init {
        inflate(context, R.layout.item_activity_tab, this)

        recyclerView.adapter = adapter
    }

    fun setActivityTab(activityTab: ActivityTab) {
        titleTextView.text = activityTab.title.getString(context)

        recyclerView.isVisible = activityTab.items.isNotEmpty()
        adapter.submitList(activityTab.items)
    }

    companion object {
        private val mediaItemDiffCallback = object : DiffUtil.ItemCallback<MediaItem<*>>() {
            override fun areItemsTheSame(
                oldItem: MediaItem<*>,
                newItem: MediaItem<*>,
            ) = oldItem.areItemsTheSame(newItem)

            override fun areContentsTheSame(
                oldItem: MediaItem<*>,
                newItem: MediaItem<*>,
            ) = oldItem.areContentsTheSame(newItem)
        }
    }
}
