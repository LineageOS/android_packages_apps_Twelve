/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.repositories.MediaRepository

class PlaylistsViewModel(application: Application) : TwelveViewModel(application) {
    private val _sortingRule = MutableStateFlow(MediaRepository.defaultPlaylistsSortingRule)
    val sortingRule = _sortingRule.asStateFlow()

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
        _sortingRule.value = sortingRule
    }

    suspend fun createPlaylist(name: String) {
        mediaRepository.navigationProvider.value?.let {
            withContext(Dispatchers.IO) {
                mediaRepository.createPlaylist(it, name)
            }
        }
    }
}
