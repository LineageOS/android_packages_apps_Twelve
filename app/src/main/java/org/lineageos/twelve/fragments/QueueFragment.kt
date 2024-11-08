/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.updatePadding
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter
import org.lineageos.twelve.ui.views.ListItem
import org.lineageos.twelve.utils.TimestampFormatter
import org.lineageos.twelve.viewmodels.QueueViewModel

/**
 * Playback service queue.
 */
@androidx.annotation.OptIn(UnstableApi::class)
class QueueFragment : Fragment(R.layout.fragment_queue) {
    // View models
    private val viewModel by viewModels<QueueViewModel>()

    // Views
    private val noElementsNestedScrollView by getViewProperty<NestedScrollView>(R.id.noElementsNestedScrollView)
    private val recyclerView by getViewProperty<RecyclerView>(R.id.recyclerView)
    private val toolbar by getViewProperty<MaterialToolbar>(R.id.toolbar)

    // RecyclerView
    private val adapter by lazy {
        object : SimpleListAdapter<MediaItem, ListItem>(
            diffCallback,
            ::ListItem,
        ) {
            override fun ViewHolder.onPrepareView() {
                view.setLeadingIconImage(R.drawable.ic_music_note)
            }

            override fun ViewHolder.onBindView(item: MediaItem) {
                view.headlineText = item.mediaMetadata.title
                view.supportingText = item.mediaMetadata.artist
                view.trailingSupportingText = item.mediaMetadata.durationMs?.let {
                    TimestampFormatter.formatTimestampMillis(it)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())

            v.updatePadding(
                insets,
                start = true,
                end = true,
            )

            windowInsets
        }

        ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                insets,
                bottom = true,
            )

            windowInsets
        }

        ViewCompat.setOnApplyWindowInsetsListener(noElementsNestedScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                insets,
                bottom = true,
            )

            windowInsets
        }

        toolbar.setupWithNavController(findNavController())

        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.queue.collectLatest {
                    adapter.submitList(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        recyclerView.adapter = null

        super.onDestroyView()
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<MediaItem>() {
            override fun areItemsTheSame(
                oldItem: MediaItem,
                newItem: MediaItem,
            ) = oldItem.mediaId == newItem.mediaId

            override fun areContentsTheSame(
                oldItem: MediaItem,
                newItem: MediaItem,
            ) = oldItem == newItem
        }
    }
}
