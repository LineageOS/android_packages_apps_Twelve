/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.ext.applicationContext
import org.lineageos.twelve.services.InfoAudioProcessor
import org.lineageos.twelve.services.ProxyDefaultAudioTrackBufferSizeProvider
import org.lineageos.twelve.utils.AudioQualityClassifier

class NowPlayingStatsViewModel(application: Application) : NowPlayingViewModel(application) {
    /**
     * Media router.
     */
    private val mediaRouter = MediaRouter.getInstance(applicationContext)

    /**
     * Current route.
     */
    private val currentRouteInfo = callbackFlow {
        val callback = object : MediaRouter.Callback() {
            override fun onRouteSelected(
                router: MediaRouter,
                route: MediaRouter.RouteInfo,
                reason: Int
            ) {
                super.onRouteSelected(router, route, reason)

                trySend(route)
            }
        }

        val mediaRouteSelector = MediaRouteSelector.Builder()
            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
            .build()

        mediaRouter.addCallback(mediaRouteSelector, callback)
        trySend(mediaRouter.getSelectedRoute())

        awaitClose {
            mediaRouter.removeCallback(callback)
        }
    }
        .flowOn(Dispatchers.Main)
        .shareIn(
            viewModelScope,
            started = SharingStarted.Eagerly,
            replay = 1,
        )

    /**
     * Whether the output is in non-passthrough PCM float mode.
     * This means the audio sink is ignoring all the processors.
     */
    @androidx.annotation.OptIn(UnstableApi::class)
    val transcodingFloatModeEnabled = combine(
        ProxyDefaultAudioTrackBufferSizeProvider.encodingFlow,
        ProxyDefaultAudioTrackBufferSizeProvider.outputModeFlow,
    ) { encoding, outputMode ->
        outputMode == DefaultAudioSink.OUTPUT_MODE_PCM && encoding == C.ENCODING_PCM_FLOAT
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    val transcodingEncoding = ProxyDefaultAudioTrackBufferSizeProvider.encodingFlow
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    val transcodingOutputMode = ProxyDefaultAudioTrackBufferSizeProvider.outputModeFlow
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    val transcodingBitrate = ProxyDefaultAudioTrackBufferSizeProvider.bitrateFlow

    /**
     * Whether the output has valid information.
     */
    @androidx.annotation.OptIn(UnstableApi::class)
    val hasOutputInformation = combine(
        ProxyDefaultAudioTrackBufferSizeProvider.outputModeFlow,
        transcodingFloatModeEnabled,
    ) { outputMode, transcodingFloatModeEnabled ->
        outputMode == DefaultAudioSink.OUTPUT_MODE_PCM && transcodingFloatModeEnabled != true
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    /**
     * The output audio stream information.
     */
    @androidx.annotation.OptIn(UnstableApi::class)
    val outputAudioFormat = combine(
        InfoAudioProcessor.audioFormatFlow,
        hasOutputInformation,
    ) { audioFormat, hasOutputInformation ->
        audioFormat?.takeIf { hasOutputInformation != false }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    @androidx.annotation.OptIn(UnstableApi::class)
    val outputConfiguration = combine(
        currentTrackFormat,
        transcodingEncoding,
        transcodingOutputMode,
        transcodingBitrate,
        outputAudioFormat,
    ) { currentTrackFormat, transcodingEncoding, transcodingOutputMode, transcodingBitrate, outputAudioFormat ->
        AudioQualityClassifier.classify(
            sourceFormat = currentTrackFormat,
            transcodingEncoding = transcodingEncoding,
            transcodingOutputMode = transcodingOutputMode,
            transcodingBitrate = transcodingBitrate,
            outputAudioFormat = outputAudioFormat,
        )
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )
}
