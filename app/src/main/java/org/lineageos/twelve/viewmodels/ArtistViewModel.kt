/*
 * SPDX-FileCopyrightText: The LineageOS Project
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
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.FlowResult
import org.lineageos.twelve.models.FlowResult.Companion.asFlowResult
import org.lineageos.twelve.models.FlowResult.Companion.getOrNull

class ArtistViewModel(application: Application) : TwelveViewModel(application) {
    private val artistUri = MutableStateFlow<Uri?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val artist = artistUri
        .filterNotNull()
        .flatMapLatest {
            mediaRepository.artist(it)
        }
        .asFlowResult()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            FlowResult.Loading
        )

    fun loadAlbum(artistUri: Uri) {
        this.artistUri.value = artistUri
    }

    fun playArtist(startFrom: Audio? = null) {
        artist.value.getOrNull()?.second?.audios?.takeUnless { it.isEmpty() }?.let { audios ->
            playAudio(audios, startFrom?.let { audios.indexOf(it) } ?: 0)
        }
    }

    fun shufflePlayArtist() {
        artist.value.getOrNull()?.second?.audios?.takeUnless { it.isEmpty() }?.let { audios ->
            playAudio(audios.shuffled(), 0)
        }
    }

    fun shufflePlayFavorites() {
        artist.value.getOrNull()?.second?.audios?.filter { it.isFavorite == true }
            ?.takeUnless { it.isEmpty() }?.let { audios ->
                playAudio(audios.shuffled(), 0)
            }
    }
}
