/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
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

class ActivityTabView(context: Context) : FrameLayout(context) {
    // Views
    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.recyclerView) }
    private val titleTextView by lazy { findViewById<TextView>(R.id.titleTextView) }

    // RecyclerView
    private val adapter = object : ListAdapter<MediaItem<*>, ActivityTabItem>(
        mediaItemDiffCallback,
    ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ActivityTabItem(
            LayoutInflater.from(parent.context).inflate(R.layout.item_activity_tab, parent, false)
        ).apply {
            itemView.setOnClickListener {
                onItemClickListener(currentList, bindingAdapterPosition)
            }
            itemView.setOnLongClickListener {
                onItemLongClickListener(currentList, bindingAdapterPosition)
            }
        }

        override fun onBindViewHolder(holder: ActivityTabItem, position: Int) {
            holder.setItem(getItem(position))
        }
    }

    // Callbacks
    private var onItemClickListener: (items: List<MediaItem<*>>, position: Int) -> Unit =
        { _, _ -> }
    private var onItemLongClickListener: (items: List<MediaItem<*>>, position: Int) -> Boolean =
        { _, _ -> false }

    init {
        inflate(context, R.layout.view_activity_tab, this)

        recyclerView.layoutManager = CarouselLayoutManager()
        CarouselSnapHelper(false).attachToRecyclerView(recyclerView)
        recyclerView.adapter = adapter
    }

    fun setOnItemClickListener(listener: ((items: List<MediaItem<*>>, position: Int) -> Unit)?) {
        onItemClickListener = listener ?: { _, _ -> }
    }

    fun setOnItemLongClickListener(
        listener: ((items: List<MediaItem<*>>, position: Int) -> Boolean)?
    ) {
        onItemLongClickListener = listener ?: { _, _ -> false }
    }

    fun setActivityTab(activityTab: ActivityTab) {
        titleTextView.text = activityTab.title.getString(context)

        adapter.submitList(activityTab.items)
        recyclerView.isVisible = activityTab.items.isNotEmpty()
    }

    companion object {
        private val mediaItemDiffCallback = object : DiffUtil.ItemCallback<MediaItem<*>>() {
            override fun areItemsTheSame(
                oldItem: MediaItem<*>,
                newItem: MediaItem<*>,
            ) = when (oldItem) {
                is Album -> oldItem.areItemsTheSame<Album>(newItem)
                is Artist -> oldItem.areItemsTheSame<Artist>(newItem)
                is Audio -> oldItem.areItemsTheSame<Audio>(newItem)
                is Genre -> oldItem.areItemsTheSame<Genre>(newItem)
                is Playlist -> oldItem.areItemsTheSame<Playlist>(newItem)
            }

            override fun areContentsTheSame(
                oldItem: MediaItem<*>,
                newItem: MediaItem<*>,
            ) = when (oldItem) {
                is Album -> oldItem.areContentsTheSame<Album>(newItem)
                is Artist -> oldItem.areContentsTheSame<Artist>(newItem)
                is Audio -> oldItem.areContentsTheSame<Audio>(newItem)
                is Genre -> oldItem.areContentsTheSame<Genre>(newItem)
                is Playlist -> oldItem.areContentsTheSame<Playlist>(newItem)
            }
        }
    }
}
