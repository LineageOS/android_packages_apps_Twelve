/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.RequestStatus

open class AudioViewModel(application: Application) : TwelveViewModel(application) {
    protected val audioUri = MutableStateFlow<Uri?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val audio = audioUri
        .filterNotNull()
        .flatMapLatest {
            mediaRepository.audio(it)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            RequestStatus.Loading()
        )

    fun loadAudio(audioUri: Uri) {
        this.audioUri.value = audioUri
    }

    fun addToQueue(audio: Audio) {
        mediaController.value?.apply {
            addMediaItem(audio.toMedia3MediaItem())

            // If the added item is the only one, play it
            if (mediaItemCount == 1) {
                play()
            }
        }
    }

    fun playNext(audio: Audio) {
        mediaController.value?.apply {
            addMediaItem(
                currentMediaItemIndex + 1,
                audio.toMedia3MediaItem()
            )

            // If the added item is the only one, play it
            if (mediaItemCount == 1) {
                play()
            }
        }
    }

    suspend fun addToPlaylist(playlistUri: Uri) {
        audioUri.value?.let {
            withContext(Dispatchers.IO) {
                mediaRepository.addAudioToPlaylist(playlistUri, it)
            }
        }
    }

    suspend fun removeFromPlaylist(playlistUri: Uri) {
        audioUri.value?.let {
            withContext(Dispatchers.IO) {
                mediaRepository.removeAudioFromPlaylist(playlistUri, it)
            }
        }
    }
}
