/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.lineageos.twelve.datasources.MediaError
import org.lineageos.twelve.ext.PLAYLISTS_SORTING_REVERSE_KEY
import org.lineageos.twelve.ext.PLAYLISTS_SORTING_STRATEGY_KEY
import org.lineageos.twelve.ext.playlistsSortingRule
import org.lineageos.twelve.ext.preferenceFlow
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.utils.M3UParser
import java.io.InputStream

class PlaylistsViewModel(application: Application) : TwelveViewModel(application) {
    val sortingRule = sharedPreferences.preferenceFlow(
        PLAYLISTS_SORTING_STRATEGY_KEY,
        PLAYLISTS_SORTING_REVERSE_KEY,
        getter = SharedPreferences::playlistsSortingRule,
    )
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            sharedPreferences.playlistsSortingRule
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlists = sortingRule
        .flatMapLatest { mediaRepository.playlists(it) }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            RequestStatus.Loading()
        )

    fun setSortingRule(sortingRule: SortingRule) {
        sharedPreferences.playlistsSortingRule = sortingRule
    }

    suspend fun createPlaylist(name: String) = mediaRepository.navigationProvider.value?.let {
        withContext(Dispatchers.IO) {
            mediaRepository.createPlaylist(it, name)
        }
    }

    suspend fun importPlaylist(
        name: String,
        inputStream: InputStream,
    ) = mediaRepository.navigationProvider.value?.let {
        withContext(Dispatchers.IO) {
            M3UParser.parse(inputStream)?.let { playlist ->
                mediaRepository.importPlaylist(it, playlist.displayTitle ?: name, playlist)
            } ?: RequestStatus.Error(MediaError.DESERIALIZATION)
        }
    } ?: RequestStatus.Error(MediaError.IO)
}
