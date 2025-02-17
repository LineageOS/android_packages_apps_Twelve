/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.updatePadding
import org.lineageos.twelve.models.Lyrics
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter
import org.lineageos.twelve.viewmodels.LyricsViewModel

/**
 * Show lyrics of currently playing audio.
 */
class LyricsFragment : Fragment(R.layout.fragment_lyrics) {
    // View models
    private val viewModel by viewModels<LyricsViewModel>()

    // Views
    private val noElementsNestedScrollView by getViewProperty<NestedScrollView>(R.id.noElementsNestedScrollView)
    private val recyclerView by getViewProperty<RecyclerView>(R.id.recyclerView)
    private val toolbar by getViewProperty<MaterialToolbar>(R.id.toolbar)

    // RecyclerView
    private val adapter by lazy {
        object : SimpleListAdapter<Pair<Lyrics.Line, LyricsViewModel.LineState>, TextView>(
            diffCallback,
            { layoutInflater.inflate(R.layout.lyrics_line, null, false) as TextView }
        ) {
            override fun ViewHolder.onBindView(item: Pair<Lyrics.Line, LyricsViewModel.LineState>) {
                val (line, lineState) = item

                view.text = line.line
                view.isSelected = lineState == LyricsViewModel.LineState.ACTIVE
                view.isActivated = lineState != LyricsViewModel.LineState.PAST
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
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())

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
                loadData()
            }
        }
    }

    override fun onDestroyView() {
        recyclerView.adapter = null

        super.onDestroyView()
    }

    private fun CoroutineScope.loadData() {
        launch {
            viewModel.lyricsWithState.collectLatest {
                when (it) {
                    is RequestStatus.Loading -> {
                        // Do nothing
                    }

                    is RequestStatus.Success -> {
                        val (lyricsWithState, currentIndex) = it.data

                        adapter.submitList(lyricsWithState)
                        recyclerView.scrollToPosition(currentIndex)

                        val isEmpty = lyricsWithState.isEmpty()

                        recyclerView.isVisible = !isEmpty
                        noElementsNestedScrollView.isVisible = isEmpty
                    }

                    is RequestStatus.Error -> {
                        Log.e(
                            LOG_TAG,
                            "Error while loading lyrics, error: ${it.error}",
                            it.throwable
                        )

                        adapter.submitList(null)

                        recyclerView.isVisible = false
                        noElementsNestedScrollView.isVisible = true
                    }
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = LyricsFragment::class.simpleName!!

        private val diffCallback =
            object : DiffUtil.ItemCallback<Pair<Lyrics.Line, LyricsViewModel.LineState>>() {
                override fun areItemsTheSame(
                    oldItem: Pair<Lyrics.Line, LyricsViewModel.LineState>,
                    newItem: Pair<Lyrics.Line, LyricsViewModel.LineState>
                ) = oldItem.first == newItem.first

                override fun areContentsTheSame(
                    oldItem: Pair<Lyrics.Line, LyricsViewModel.LineState>,
                    newItem: Pair<Lyrics.Line, LyricsViewModel.LineState>
                ) = oldItem.second == newItem.second
            }
    }
}
