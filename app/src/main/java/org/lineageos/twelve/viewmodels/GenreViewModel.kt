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

class GenreViewModel(application: Application) : TwelveViewModel(application) {
    private val genreUri = MutableStateFlow<Uri?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val genre = genreUri
        .filterNotNull()
        .flatMapLatest {
            mediaRepository.genre(it)
        }
        .asFlowResult()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            FlowResult.Loading
        )

    fun loadGenre(genreUri: Uri) {
        this.genreUri.value = genreUri
    }

    fun playGenre(startFrom: Audio? = null) {
        genre.value.getOrNull()?.second?.audios?.takeUnless { it.isEmpty() }?.let { audios ->
            playAudio(audios, startFrom?.let { audios.indexOf(it) } ?: 0)
        }
    }

    fun shufflePlayGenre() {
        genre.value.getOrNull()?.second?.audios?.takeUnless { it.isEmpty() }?.let { audios ->
            playAudio(audios.shuffled(), 0)
        }
    }

    fun shufflePlayFavorites() {
        genre.value.getOrNull()?.second?.audios?.filter { it.isFavorite == true }
            ?.takeUnless { it.isEmpty() }?.let { audios ->
                playAudio(audios.shuffled(), 0)
            }
    }
}
