/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.lineageos.twelve.models.RequestStatus

class PlaylistsViewModel(application: Application) : TwelveViewModel(application) {
    val playlists = mediaRepository.playlists()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            RequestStatus.Loading()
        )

    suspend fun createPlaylist(name: String) {
        withContext(Dispatchers.IO) {
            mediaRepository.createPlaylist(mediaRepository.navigationProvider.value, name)
        }
    }
}
