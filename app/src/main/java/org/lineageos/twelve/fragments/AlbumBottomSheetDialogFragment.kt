/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.datasources.MediaError
import org.lineageos.twelve.ext.getParcelable
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.navigateSafe
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.ui.views.FullscreenLoadingProgressBar
import org.lineageos.twelve.ui.views.ListItem
import org.lineageos.twelve.utils.PermissionsChecker
import org.lineageos.twelve.utils.PermissionsUtils
import org.lineageos.twelve.viewmodels.AlbumViewModel

/**
 * Audio information.
 */
class AlbumBottomSheetDialogFragment : BottomSheetDialogFragment(
    R.layout.fragment_album_bottom_sheet_dialog
) {
    // View models
    private val viewModel by viewModels<AlbumViewModel>()

    // Views
    private val addToQueueListItem by getViewProperty<ListItem>(R.id.addToQueueListItem)
    private val albumTitleTextView by getViewProperty<TextView>(R.id.titleTextView)
    private val artistNameTextView by getViewProperty<TextView>(R.id.artistNameTextView)
    private val openArtistListItem by getViewProperty<ListItem>(R.id.openArtistListItem)
    private val playNextListItem by getViewProperty<ListItem>(R.id.playNextListItem)

    // Arguments
    private val albumUri: Uri
        get() = requireArguments().getParcelable(ARG_ALBUM_URI, Uri::class)!!
    private val fromArtist: Boolean
        get() = requireArguments().getBoolean(ARG_FROM_ARTIST)

    // Permissions
    private val permissionsChecker = PermissionsChecker(
        this, PermissionsUtils.mainPermissions
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        openArtistListItem.isVisible = !fromArtist

        viewModel.loadAlbum(albumUri)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsChecker.withPermissionsGranted {
                    loadData()
                }
            }
        }
    }

    private suspend fun loadData() {
        viewModel.album.collect {
            when (it) {
                is RequestStatus.Loading -> {
                    // Do nothing
                }

                is RequestStatus.Success -> {
                    val album = it.data.first
                    val tracks = it.data.second

                    albumTitleTextView.text = album.title
                    artistNameTextView.text = album.artistName

                    addToQueueListItem.setOnClickListener {
                        viewModel.addToQueue(tracks)

                        findNavController().navigateUp()
                    }

                    playNextListItem.setOnClickListener {
                        viewModel.playNext(tracks)

                        findNavController().navigateUp()
                    }

                    openArtistListItem.setOnClickListener {
                        findNavController().navigateSafe(
                            R.id.action_albumBottomSheetDialogFragment_to_fragment_artist,
                            ArtistFragment.createBundle(album.artistUri)
                        )
                    }
                }

                is RequestStatus.Error -> {
                    Log.e(LOG_TAG, "Failed to load album, error: ${it.error}")

                    if (it.error == MediaError.NOT_FOUND) {
                        // Get out of here
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = AlbumBottomSheetDialogFragment::class.simpleName!!

        private const val ARG_ALBUM_URI = "album_uri"
        private const val ARG_FROM_ARTIST = "from_artist"

        /**
         * Create a [Bundle] to use as the arguments for this fragment.
         * @param albumUri The URI of the album to display
         * @param fromArtist Whether this fragment was opened from an artist
         */
        fun createBundle(
            albumUri: Uri,
            fromArtist: Boolean = false,
        ) = bundleOf(
            ARG_ALBUM_URI to albumUri,
            ARG_FROM_ARTIST to fromArtist,
        )
    }
}
