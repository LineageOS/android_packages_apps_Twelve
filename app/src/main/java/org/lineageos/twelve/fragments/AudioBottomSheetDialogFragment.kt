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
import org.lineageos.twelve.viewmodels.AudioViewModel

/**
 * Audio information.
 */
class AudioBottomSheetDialogFragment : BottomSheetDialogFragment(
    R.layout.fragment_audio_bottom_sheet_dialog
) {
    // View models
    private val viewModel by viewModels<AudioViewModel>()

    // Views
    private val addOrRemoveFromPlaylistsListItem by getViewProperty<ListItem>(R.id.addOrRemoveFromPlaylistsListItem)
    private val addToQueueListItem by getViewProperty<ListItem>(R.id.addToQueueListItem)
    private val artistNameTextView by getViewProperty<TextView>(R.id.artistNameTextView)
    private val albumTitleTextView by getViewProperty<TextView>(R.id.albumTitleTextView)
    private val fullscreenLoadingProgressBar by getViewProperty<FullscreenLoadingProgressBar>(R.id.fullscreenLoadingProgressBar)
    private val openAlbumListItem by getViewProperty<ListItem>(R.id.openAlbumListItem)
    private val openArtistListItem by getViewProperty<ListItem>(R.id.openArtistListItem)
    private val openGenreListItem by getViewProperty<ListItem>(R.id.openGenreListItem)
    private val playNextListItem by getViewProperty<ListItem>(R.id.playNextListItem)
    private val removeFromPlaylistListItem by getViewProperty<ListItem>(R.id.removeFromPlaylistListItem)
    private val titleTextView by getViewProperty<TextView>(R.id.titleTextView)

    // Arguments
    private val audioUri: Uri
        get() = requireArguments().getParcelable(ARG_AUDIO_URI, Uri::class)!!
    private val fromAlbum: Boolean
        get() = requireArguments().getBoolean(ARG_FROM_ALBUM)
    private val fromArtist: Boolean
        get() = requireArguments().getBoolean(ARG_FROM_ARTIST)
    private val fromGenre: Boolean
        get() = requireArguments().getBoolean(ARG_FROM_GENRE)
    private val playlistUri: Uri?
        get() = requireArguments().getParcelable(ARG_PLAYLIST_URI, Uri::class)

    // Permissions
    private val permissionsChecker = PermissionsChecker(
        this, PermissionsUtils.mainPermissions
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        removeFromPlaylistListItem.isVisible = playlistUri != null
        removeFromPlaylistListItem.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                fullscreenLoadingProgressBar.withProgress {
                    playlistUri?.let {
                        viewModel.removeFromPlaylist(it)

                        findNavController().navigateUp()
                    }
                }
            }
        }

        addOrRemoveFromPlaylistsListItem.setOnClickListener {
            findNavController().navigateSafe(
                R.id.action_audioBottomSheetDialogFragment_to_fragment_add_or_remove_from_playlists,
                AddOrRemoveFromPlaylistsFragment.createBundle(audioUri)
            )
        }

        openAlbumListItem.isVisible = !fromAlbum

        openArtistListItem.isVisible = !fromArtist

        viewModel.loadAudio(audioUri)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsChecker.withPermissionsGranted {
                    loadData()
                }
            }
        }
    }

    private suspend fun loadData() {
        viewModel.audio.collect {
            when (it) {
                is RequestStatus.Loading -> {
                    // Do nothing
                }

                is RequestStatus.Success -> {
                    val audio = it.data

                    titleTextView.text = audio.title
                    artistNameTextView.text = audio.artistName
                    albumTitleTextView.text = audio.albumTitle

                    addToQueueListItem.setOnClickListener {
                        viewModel.addToQueue(audio)

                        findNavController().navigateUp()
                    }

                    playNextListItem.setOnClickListener {
                        viewModel.playNext(audio)

                        findNavController().navigateUp()
                    }

                    openAlbumListItem.setOnClickListener {
                        findNavController().navigateSafe(
                            R.id.action_audioBottomSheetDialogFragment_to_fragment_album,
                            AlbumFragment.createBundle(audio.albumUri)
                        )
                    }

                    openArtistListItem.setOnClickListener {
                        findNavController().navigateSafe(
                            R.id.action_audioBottomSheetDialogFragment_to_fragment_artist,
                            ArtistFragment.createBundle(audio.artistUri)
                        )
                    }

                    openGenreListItem.isVisible = !fromGenre && audio.genreUri != null
                    openGenreListItem.setOnClickListener {
                        audio.genreUri?.let { genreUri ->
                            findNavController().navigateSafe(
                                R.id.action_audioBottomSheetDialogFragment_to_fragment_genre,
                                GenreFragment.createBundle(genreUri)
                            )
                        }
                    }
                }

                is RequestStatus.Error -> {
                    Log.e(LOG_TAG, "Failed to load audio, error: ${it.error}")

                    if (it.error == MediaError.NOT_FOUND) {
                        // Get out of here
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = AudioBottomSheetDialogFragment::class.simpleName!!

        private const val ARG_AUDIO_URI = "audio_uri"
        private const val ARG_FROM_ALBUM = "from_album"
        private const val ARG_FROM_ARTIST = "from_artist"
        private const val ARG_FROM_GENRE = "from_genre"
        private const val ARG_PLAYLIST_URI = "playlist_uri"

        /**
         * Create a [Bundle] to use as the arguments for this fragment.
         * @param audioUri The URI of the audio to display
         * @param fromAlbum Whether this fragment was opened from an album
         * @param fromArtist Whether this fragment was opened from an artist
         * @param fromGenre Whether this fragment was opened from a genre
         * @param playlistUri If the audio has been opened from a playlist, the URI of the playlist
         */
        fun createBundle(
            audioUri: Uri,
            fromAlbum: Boolean = false,
            fromArtist: Boolean = false,
            fromGenre: Boolean = false,
            playlistUri: Uri? = null,
        ) = bundleOf(
            ARG_AUDIO_URI to audioUri,
            ARG_FROM_ALBUM to fromAlbum,
            ARG_FROM_ARTIST to fromArtist,
            ARG_FROM_GENRE to fromGenre,
            ARG_PLAYLIST_URI to playlistUri,
        )
    }
}
