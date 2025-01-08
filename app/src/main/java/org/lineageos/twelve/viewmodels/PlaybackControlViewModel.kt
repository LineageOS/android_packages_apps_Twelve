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

    fun increasePlaybackSpeed() {
        val newSpeed = playbackParameters.value.speed + SPEED_STEP
        mediaController.value?.setPlaybackParameters(
            playbackParameters.value.withSpeed(newSpeed.coerceAtMost(MAX_SPEED))
        )
    }

    fun decreasePlaybackSpeed() {
        val newSpeed = playbackParameters.value.speed - SPEED_STEP
        mediaController.value?.setPlaybackParameters(
            playbackParameters.value.withSpeed(newSpeed.coerceAtLeast(MIN_SPEED))
        )
    }

    fun resetPlaybackSpeed() {
        mediaController.value?.setPlaybackParameters(
            playbackParameters.value.withSpeed(1f)
        )
    }

    fun setPlaybackPitch(pitch: Float) {
        mediaController.value?.setPlaybackParameters(
            playbackParameters.value.withPitch(pitch)
        )
    }

    companion object {
        private const val MIN_SPEED = 0.5f
        private const val MAX_SPEED = 2.5f
        private const val SPEED_STEP = 0.25f
    }
}
