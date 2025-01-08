/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.ext.playbackParametersFlow
import org.lineageos.twelve.ext.withPitch

class PlaybackControlViewModel(application: Application) : TwelveViewModel(application) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val playbackParameters = mediaController
        .filterNotNull()
        .flatMapLatest { it.playbackParametersFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = PlaybackParameters(1f, 1f)
        )

    fun setPlaybackSpeed(speed: Float) {
        mediaController.value?.setPlaybackParameters(
            playbackParameters.value.withSpeed(speed)
        )
    }

    fun setPlaybackPitch(pitch: Float) {
        mediaController.value?.setPlaybackParameters(
            playbackParameters.value.withPitch(pitch)
        )
    }
}
