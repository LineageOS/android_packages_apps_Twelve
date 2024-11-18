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
import org.lineageos.twelve.models.MediaItem
import org.lineageos.twelve.models.areContentsTheSame
import org.lineageos.twelve.models.areItemsTheSame
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter

class ActivityTabView(context: Context) : FrameLayout(context) {
    // Views
    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.recyclerView) }
    private val titleTextView by lazy { findViewById<TextView>(R.id.titleTextView) }

    // RecyclerView
    private val adapter = object : SimpleListAdapter<MediaItem<*>, ActivityTabItem>(
        mediaItemDiffCallback,
        ::ActivityTabItem,
    ) {
        override fun ViewHolder.onPrepareView() {
            view.setOnClickListener {
                onItemClickListener(currentList, bindingAdapterPosition)
            }
            view.setOnLongClickListener {
                onItemLongClickListener(currentList, bindingAdapterPosition)
            }
        }

        override fun ViewHolder.onBindView(item: MediaItem<*>) {
            view.setItem(item)
        }
    }

    // Callbacks
    private var onItemClickListener: (items: List<MediaItem<*>>, position: Int) -> Unit =
        { _, _ -> }
    private var onItemLongClickListener: (items: List<MediaItem<*>>, position: Int) -> Boolean =
        { _, _ -> false }

    init {
        inflate(context, R.layout.view_activity_tab, this)

        recyclerView.adapter = adapter
    }

    fun setOnItemClickListener(listener: ((items: List<MediaItem<*>>, position: Int) -> Unit)?) {
        onItemClickListener = listener ?: { _, _ -> }
    }

    fun setOnItemLongClickListener(
        listener: ((items: List<MediaItem<*>>, position: Int) -> Boolean)?,
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
            ) = oldItem.areItemsTheSame(newItem)

            override fun areContentsTheSame(
                oldItem: MediaItem<*>,
                newItem: MediaItem<*>,
            ) = oldItem.areContentsTheSame(newItem)
        }
    }
}
