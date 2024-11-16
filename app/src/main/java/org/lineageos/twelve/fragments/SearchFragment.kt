/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.search.SearchView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.scheduleHideSoftInput
import org.lineageos.twelve.ext.setProgressCompat
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.MediaItem
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.areContentsTheSame
import org.lineageos.twelve.models.areItemsTheSame
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter
import org.lineageos.twelve.ui.views.ListItem
import org.lineageos.twelve.utils.PermissionsChecker
import org.lineageos.twelve.utils.PermissionsUtils
import org.lineageos.twelve.viewmodels.SearchViewModel

/**
 * Search across all contents.
 */
class SearchFragment : Fragment(R.layout.fragment_search) {
    // View models
    private val viewModel by viewModels<SearchViewModel>()

    // Views
    private val linearProgressIndicator by getViewProperty<LinearProgressIndicator>(R.id.linearProgressIndicator)
    private val noElementsLinearLayout by getViewProperty<LinearLayout>(R.id.noElementsLinearLayout)
    private val recyclerView by getViewProperty<RecyclerView>(R.id.recyclerView)
    private val searchView by getViewProperty<SearchView>(R.id.searchView)

    // System services
    private val inputMethodManager: InputMethodManager
        get() = requireContext().getSystemService(InputMethodManager::class.java)

    // Recyclerview
    private val adapter by lazy {
        object : SimpleListAdapter<MediaItem<*>, ListItem>(diffCallback, ::ListItem) {
            override fun ViewHolder.onPrepareView() {
                view.setOnClickListener {
                    item?.let {
                        when (it) {
                            is Album -> findNavController().navigate(
                                R.id.action_mainFragment_to_fragment_album,
                                AlbumFragment.createBundle(it.uri)
                            )

                            is Artist -> findNavController().navigate(
                                R.id.action_mainFragment_to_fragment_artist,
                                ArtistFragment.createBundle(it.uri)
                            )

                            is Audio -> with(currentList.filterIsInstance<Audio>()) {
                                viewModel.playAudio(
                                    this, indexOf(it)
                                )

                                findNavController().navigate(
                                    R.id.action_mainFragment_to_fragment_now_playing
                                )
                            }

                            else -> {}
                        }
                    }
                }
            }

            override fun ViewHolder.onBindView(item: MediaItem<*>) {
                when (item) {
                    is Album -> {
                        view.setTrailingIconImage(R.drawable.ic_album)
                        view.headlineText = item.title
                        view.supportingText = item.artistName
                    }

                    is Artist -> {
                        view.setTrailingIconImage(R.drawable.ic_person)
                        view.headlineText = item.name
                    }

                    is Audio -> {
                        view.setTrailingIconImage(R.drawable.ic_music_note)
                        view.headlineText = item.title
                        view.supportingText = item.artistName
                    }

                    is Genre -> {
                        view.setTrailingIconImage(R.drawable.ic_genres)
                        view.headlineText = item.name
                    }

                    is Playlist -> {
                        view.setTrailingIconImage(R.drawable.ic_playlist_play)
                        view.headlineText = item.name
                    }
                }
            }
        }
    }

    // Permissions
    private val permissionsChecker = PermissionsChecker(
        this, PermissionsUtils.mainPermissions
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.adapter = adapter

        // This library sucks.
        @Suppress("RestrictedApi")
        searchView.setStatusBarSpacerEnabled(false)

        searchView.editText.addTextChangedListener { text ->
            viewModel.setSearchQuery(text.toString())
        }
        searchView.editText.setOnEditorActionListener { _, _, _ ->
            inputMethodManager.scheduleHideSoftInput(searchView.editText, 0)
            searchView.editText.clearFocus()
            viewModel.setSearchQuery(searchView.editText.text.toString(), true)
            true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsChecker.withPermissionsGranted {
                    loadData()
                }
            }
        }
    }

    override fun onDestroyView() {
        recyclerView.adapter = null

        super.onDestroyView()
    }

    private suspend fun loadData() {
        viewModel.searchResults.collectLatest {
            linearProgressIndicator.setProgressCompat(it, true)

            when (it) {
                is RequestStatus.Loading -> {
                    // Do nothing
                }

                is RequestStatus.Success -> {
                    adapter.submitList(it.data)

                    val isEmpty = it.data.isEmpty()
                    recyclerView.isVisible = !isEmpty
                    noElementsLinearLayout.isVisible =
                        isEmpty && searchView.editText.text.isNotEmpty()
                }

                is RequestStatus.Error -> {
                    Log.e(LOG_TAG, "Failed to load search results, error: ${it.error}")

                    adapter.submitList(listOf())

                    recyclerView.isVisible = false
                    noElementsLinearLayout.isVisible = true
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = SearchFragment::class.simpleName!!

        private val diffCallback = object : DiffUtil.ItemCallback<MediaItem<*>>() {
            override fun areItemsTheSame(
                oldItem: MediaItem<*>,
                newItem: MediaItem<*>
            ) = when (oldItem) {
                is Album -> oldItem.areItemsTheSame(newItem)
                is Artist -> oldItem.areItemsTheSame(newItem)
                is Audio -> oldItem.areItemsTheSame(newItem)
                is Genre -> oldItem.areItemsTheSame(newItem)
                is Playlist -> oldItem.areItemsTheSame(newItem)
            }

            override fun areContentsTheSame(
                oldItem: MediaItem<*>,
                newItem: MediaItem<*>
            ) = when (oldItem) {
                is Album -> oldItem.areContentsTheSame(newItem)
                is Artist -> oldItem.areContentsTheSame(newItem)
                is Audio -> oldItem.areContentsTheSame(newItem)
                is Genre -> oldItem.areContentsTheSame(newItem)
                is Playlist -> oldItem.areContentsTheSame(newItem)
            }
        }
    }
}
