/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.models.OutputConfiguration
import org.lineageos.twelve.services.InfoAudioProcessor
import org.lineageos.twelve.services.ProxyDefaultAudioTrackBufferSizeProvider
import org.lineageos.twelve.utils.OutputConfigurationUtils
import org.lineageos.twelve.utils.OutputConfigurationUtils.toModel

class NowPlayingStatsViewModel(application: Application) : NowPlayingViewModel(application) {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val outputSource = currentTrackFormat
        .mapLatest { it?.toModel() }
        .flowOn(Dispatchers.IO)
        .shareIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            replay = 1
        )

    private val outputTranscoding = combine(
        ProxyDefaultAudioTrackBufferSizeProvider.transcodingData,
        outputConfigurationRepository.audioFormat,
    ) { transcodingData, audioFormat ->
        OutputConfigurationUtils.buildOutputTranscoding(
            transcodingData = transcodingData,
            audioFormat = audioFormat,
        )
    }
        .flowOn(Dispatchers.IO)
        .shareIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            replay = 1
        )

    /**
     * Whether the output is in non-passthrough PCM float mode.
     * This means the audio sink is ignoring all the processors.
     */
    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(ExperimentalCoroutinesApi::class)
    val transcodingFloatModeEnabled = outputTranscoding
        .mapLatest {
            it?.let {
                it.outputMode == OutputConfiguration.Transcoding.OutputMode.PCM
                        && it.encoding == OutputConfiguration.Encoding.PCM_FLOAT
            }
        }
        .flowOn(Dispatchers.IO)
        .shareIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            replay = 1
        )

    /**
     * Whether the output has valid information.
     */
    @androidx.annotation.OptIn(UnstableApi::class)
    val hasOutputInformation = combine(
        outputTranscoding,
        transcodingFloatModeEnabled,
    ) { outputTranscoding, transcodingFloatModeEnabled ->
        outputTranscoding?.outputMode == OutputConfiguration.Transcoding.OutputMode.PCM
                && transcodingFloatModeEnabled != true
    }
        .flowOn(Dispatchers.IO)
        .shareIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            replay = 1
        )

    /**
     * The output audio stream information.
     */
    @androidx.annotation.OptIn(UnstableApi::class)
    private val outputAudioFormat = combine(
        InfoAudioProcessor.audioFormatFlow,
        hasOutputInformation,
    ) { audioFormat, hasOutputInformation ->
        audioFormat?.takeIf { hasOutputInformation }
    }
        .flowOn(Dispatchers.IO)
        .shareIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            replay = 1
        )

    @androidx.annotation.OptIn(UnstableApi::class)
    val outputConfiguration = combine(
        outputSource,
        outputTranscoding,
        outputConfigurationRepository.device,
    ) { outputSource, outputTranscoding, outputDevice ->
        OutputConfigurationUtils.classify(
            source = outputSource,
            transcoding = outputTranscoding,
            device = outputDevice,
        )
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )
}
