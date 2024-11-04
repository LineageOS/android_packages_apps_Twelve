/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lineageos.twelve.models.RequestStatus

class AddOrRemoveFromPlaylistsViewModel(application: Application) : AudioViewModel(application) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val playlistToHasAudio = audioUri
        .filterNotNull()
        .flatMapLatest {
            mediaRepository.audioPlaylistsStatus(it)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            RequestStatus.Loading()
        )

    /**
     * Create a new playlist in the same provider as the audio.
     */
    suspend fun createPlaylist(name: String) {
        withContext(Dispatchers.IO) {
            audioUri.value?.let {
                mediaRepository.getProviderOfMediaItems(it)?.let { provider ->
                    mediaRepository.createPlaylist(provider, name)
                }
            }
        }
    }
}
