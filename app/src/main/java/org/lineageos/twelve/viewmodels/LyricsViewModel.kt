/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.models.Lyrics
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.RequestStatus.Companion.map

class LyricsViewModel(application: Application) : NowPlayingViewModel(application) {
    enum class LineState {
        /**
         * Line is still to be reached.
         */
        PENDING,

        /**
         * Line is relevant with current timestamp.
         */
        ACTIVE,

        /**
         * Line is past the current timestamp.
         */
        PAST,
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val lyricsWithState = currentLyricsLines
        .mapLatest {
            it.map { (lyrics, currentIndex) ->
                lyrics.mapIndexed { index, line ->
                    line to when {
                        index < currentIndex -> LineState.PAST
                        index == currentIndex -> LineState.ACTIVE
                        else -> LineState.PENDING
                    }
                } to currentIndex
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = RequestStatus.Loading()
        )

    fun seekToLine(line: Lyrics.Line) {
        mediaController.value?.apply {
            line.durationMs?.let { durationMs ->
                seekTo(durationMs.first)
            }
        }
    }
}
