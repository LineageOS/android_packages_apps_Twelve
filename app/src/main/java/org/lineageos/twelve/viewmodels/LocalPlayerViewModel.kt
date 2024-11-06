/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.TwelveApplication
import org.lineageos.twelve.ext.applicationContext
import org.lineageos.twelve.ext.isPlayingFlow
import org.lineageos.twelve.ext.mediaItemFlow
import org.lineageos.twelve.ext.mediaMetadataFlow
import org.lineageos.twelve.ext.playbackStateFlow
import org.lineageos.twelve.ext.typedRepeatMode
import org.lineageos.twelve.models.PlaybackState
import org.lineageos.twelve.models.RepeatMode
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.Thumbnail

/**
 * A view model useful to playback stuff locally (not in the playback service).
 */
class LocalPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val mediaRepository = getApplication<TwelveApplication>().mediaRepository

    // ExoPlayer
    private val exoPlayer = ExoPlayer.Builder(applicationContext)
        .build()
        .apply {
            typedRepeatMode = RepeatMode.ONE
        }

    val mediaMetadata = exoPlayer.mediaMetadataFlow()
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = MediaMetadata.EMPTY
        )

    val mediaItem = exoPlayer.mediaItemFlow()
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    val playbackState = exoPlayer.playbackStateFlow()
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    val isPlaying = exoPlayer.isPlayingFlow()
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    val mediaArtwork = combine(
        mediaMetadata,
        playbackState,
    ) { mediaMetadata, playbackState ->
        when (playbackState) {
            PlaybackState.BUFFERING -> RequestStatus.Loading()

            else -> RequestStatus.Success<_, Nothing>(
                mediaMetadata.artworkUri?.let {
                    Thumbnail(uri = it)
                } ?: mediaMetadata.artworkData?.let {
                    BitmapFactory.decodeByteArray(it, 0, it.size)?.let { bitmap ->
                        Thumbnail(bitmap = bitmap)
                    }
                }
            )
        }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = RequestStatus.Loading()
        )

    val durationCurrentPositionMs = flow {
        while (true) {
            val duration = exoPlayer.duration.takeIf { it != C.TIME_UNSET }
            emit(
                Triple(
                    duration,
                    duration?.let { exoPlayer.currentPosition },
                    exoPlayer.playbackParameters.speed,
                )
            )
            delay(200)
        }
    }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Triple(null, null, 1f)
        )

    override fun onCleared() {
        exoPlayer.release()

        super.onCleared()
    }

    fun setMediaUris(uris: Iterable<Uri>) {
        exoPlayer.apply {
            setMediaItems(
                uris.map {
                    MediaItem.fromUri(it)
                }
            )
            prepare()
            play()
        }
    }

    fun togglePlayPause() {
        exoPlayer.apply {
            if (playWhenReady) {
                pause()
            } else {
                play()
            }
        }
    }

    fun seekToPosition(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }
}
