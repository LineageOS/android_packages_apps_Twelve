/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.setProgressCompat
import org.lineageos.twelve.models.ActivityTab
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.ui.recyclerview.SimpleListAdapter
import org.lineageos.twelve.ui.recyclerview.UniqueItemDiffCallback
import org.lineageos.twelve.ui.views.ActivityTabView
import org.lineageos.twelve.utils.PermissionsChecker
import org.lineageos.twelve.utils.PermissionsUtils
import org.lineageos.twelve.viewmodels.ActivityViewModel

/**
 * User activity, notifications and recommendations.
 */
class ActivityFragment : Fragment(R.layout.fragment_activity) {
    // View models
    private val viewModel by viewModels<ActivityViewModel>()

    // Views
    private val linearProgressIndicator by getViewProperty<LinearProgressIndicator>(R.id.linearProgressIndicator)
    private val noElementsLinearLayout by getViewProperty<LinearLayout>(R.id.noElementsLinearLayout)
    private val recyclerView by getViewProperty<RecyclerView>(R.id.recyclerView)

    // RecyclerView
    private val adapter by lazy {
        object : SimpleListAdapter<ActivityTab, ActivityTabView>(
            UniqueItemDiffCallback(),
            ::ActivityTabView,
        ) {
            override fun ViewHolder.onPrepareView() {
                view.setOnItemClickListener { items, position ->
                    when (val item = items[position]) {
                        is Album -> findNavController().navigate(
                            R.id.action_mainFragment_to_fragment_album,
                            AlbumFragment.createBundle(item.uri)
                        )

                        is Artist -> findNavController().navigate(
                            R.id.action_mainFragment_to_fragment_artist,
                            ArtistFragment.createBundle(item.uri)
                        )

                        is Audio -> findNavController().navigate(
                            R.id.action_mainFragment_to_fragment_audio_bottom_sheet_dialog,
                            AudioBottomSheetDialogFragment.createBundle(item.uri)
                        )

                        is Genre -> findNavController().navigate(
                            R.id.action_mainFragment_to_fragment_genre,
                            GenreFragment.createBundle(item.uri)
                        )

                        is Playlist -> findNavController().navigate(
                            R.id.action_mainFragment_to_fragment_playlist,
                            PlaylistFragment.createBundle(item.uri)
                        )
                    }
                }
            }

            override fun ViewHolder.onBindView(item: ActivityTab) {
                view.setActivityTab(item)
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
        viewModel.activity.collectLatest {
            linearProgressIndicator.setProgressCompat(it, true)

            when (it) {
                is RequestStatus.Loading -> {
                    // Do nothing
                }

                is RequestStatus.Success -> {
                    val data = it.data

                    adapter.submitList(data)

                    val isEmpty = it.data.isEmpty()
                    recyclerView.isVisible = !isEmpty
                    noElementsLinearLayout.isVisible = isEmpty
                }

                is RequestStatus.Error -> {
                    Log.e(LOG_TAG, "Failed to load activity, error: ${it.error}")

                    recyclerView.isVisible = false
                    noElementsLinearLayout.isVisible = true
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = ActivityFragment::class.simpleName!!
    }
}
