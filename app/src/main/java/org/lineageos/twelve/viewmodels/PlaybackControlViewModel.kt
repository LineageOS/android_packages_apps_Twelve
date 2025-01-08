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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.ext.playbackParametersFlow
import org.lineageos.twelve.ext.withPitch

class PlaybackControlViewModel(application: Application) : TwelveViewModel(application) {
    private val _speedPlusButtonEnabled = MutableStateFlow(true)
    val speedPlusButtonEnabled = _speedPlusButtonEnabled

    private val _speedMinusButtonEnabled = MutableStateFlow(true)
    val speedMinusButtonEnabled = _speedMinusButtonEnabled

    private val _pitchSliderVisible = MutableStateFlow(false)
    val pitchSliderVisible = _pitchSliderVisible

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
        if (newSpeed <= SPEED_MAX) {
            mediaController.value?.setPlaybackParameters(
                playbackParameters.value.withSpeed(newSpeed)
            )
        }

        _speedPlusButtonEnabled.value = newSpeed < SPEED_MAX
    }

    fun decreasePlaybackSpeed() {
        val newSpeed = playbackParameters.value.speed - SPEED_STEP
        if (newSpeed >= SPEED_MIN) {
            mediaController.value?.setPlaybackParameters(
                playbackParameters.value.withSpeed(newSpeed)
            )
        }

        _speedMinusButtonEnabled.value = newSpeed > SPEED_MIN
    }

    fun resetPlaybackSpeed() {
        mediaController.value?.setPlaybackParameters(
            playbackParameters.value.withSpeed(SPEED_DEFAULT)
        )

        _speedPlusButtonEnabled.value = true
        _speedMinusButtonEnabled.value = true
    }

    fun togglePitchUnlock() {
        _pitchSliderVisible.value = !_pitchSliderVisible.value

        if (!_pitchSliderVisible.value) {
            mediaController.value?.setPlaybackParameters(
                playbackParameters.value.withPitch(PITCH_DEFAULT)
            )
        }
    }

    fun setPlaybackPitch(pitch: Float) {
        mediaController.value?.setPlaybackParameters(
            playbackParameters.value.withPitch(pitch)
        )
    }

    companion object {
        private const val SPEED_DEFAULT = 1f
        private const val SPEED_MIN = 0.5f
        private const val SPEED_MAX = 2.5f
        private const val SPEED_STEP = 0.25f

        private const val PITCH_DEFAULT = 1f
        private const val PITCH_MIN = 0.5f
        private const val PITCH_MAX = 1.5f

        fun sliderToPitch(sliderValue: Float, start: Float, end: Float): Float {
            val sliderRange = end - start
            val pitchRange = PITCH_MAX - PITCH_MIN
            return PITCH_MIN + ((sliderValue - start) / sliderRange) * pitchRange
        }

        fun pitchToSlider(pitchValue: Float, start: Float, end: Float): Float {
            val sliderRange = end - start
            val pitchRange = PITCH_MAX - PITCH_MIN
            return start + ((pitchValue - PITCH_MIN) / pitchRange) * sliderRange
        }
    }
}
