/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.models.FlowResult
import org.lineageos.twelve.models.FlowResult.Companion.asFlowResult

class MainViewModel(application: Application) : TwelveViewModel(application) {
    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()
    val allSongs = mediaRepository.allSongs()
        .asFlowResult()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            FlowResult.Loading(),
        )

    fun shuffleAndPlayAll() {
        viewModelScope.launch {
            val result = allSongs.first { it !is FlowResult.Loading }

            when (result) {
                is FlowResult.Success -> {
                    val tracks = result.data
                    if (tracks.isNotEmpty()) {
                        playAudio(tracks.shuffled(), 0)
                    } else {
                        _errorEvent.emit(
                            getString(getApplication(), R.string.play_all_songs_error_no_songs)
                        )
                    }
                }
                is FlowResult.Error -> {
                    _errorEvent.emit(
                        getString(getApplication(), R.string.play_all_songs_error)
                    )
                }
                is FlowResult.Loading -> Unit
            }
        }
    }
}
