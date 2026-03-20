/*
 * SPDX-FileCopyrightText: 2025-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.services

import android.media.AudioDeviceInfo
import android.media.AudioTrack
import androidx.media3.common.Format
import androidx.media3.common.util.Clock
import androidx.media3.exoplayer.analytics.PlayerId
import androidx.media3.exoplayer.audio.AudioOffloadSupport
import androidx.media3.exoplayer.audio.AudioOutputProvider
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.AudioTrackAudioOutput
import androidx.media3.exoplayer.audio.DefaultAudioSink
import java.nio.ByteBuffer

/**
 * An [AudioSink] implementation that wraps a [DefaultAudioSink] and exposes the [AudioTrack] used
 * by the [DefaultAudioSink].
 */
@androidx.media3.common.util.UnstableApi
class TwelveAudioSink(
    private val defaultAudioSink: DefaultAudioSink,
    private val onAudioTrackUpdate: (AudioTrack?) -> Unit,
) : AudioSink by defaultAudioSink {
    private val audioOutputField = DefaultAudioSink::class.java.getDeclaredField(
        "audioOutput"
    ).apply {
        isAccessible = true
    }
    private val audioTrackField = AudioTrackAudioOutput::class.java.getDeclaredField(
        "audioTrack"
    ).apply {
        isAccessible = true
    }

    private val audioTrack
        get() = audioOutputField.get(defaultAudioSink)?.let {
            audioTrackField.get(it) as AudioTrack?
        }

    override fun setPlayerId(playerId: PlayerId?) {
        defaultAudioSink.setPlayerId(playerId)
    }

    override fun setClock(clock: Clock) {
        defaultAudioSink.setClock(clock)
    }

    override fun getFormatOffloadSupport(format: Format): AudioOffloadSupport {
        return defaultAudioSink.getFormatOffloadSupport(format)
    }

    override fun handleBuffer(
        buffer: ByteBuffer,
        presentationTimeUs: Long,
        encodedAccessUnitCount: Int,
    ) = try {
        defaultAudioSink.handleBuffer(buffer, presentationTimeUs, encodedAccessUnitCount)
    } finally {
        onAudioTrackUpdate(audioTrack)
    }

    override fun setPreferredDevice(audioDeviceInfo: AudioDeviceInfo?) {
        defaultAudioSink.setPreferredDevice(audioDeviceInfo)
    }

    override fun setVirtualDeviceId(virtualDeviceId: Int) {
        defaultAudioSink.setVirtualDeviceId(virtualDeviceId)
    }

    override fun setOffloadMode(offloadMode: Int) {
        defaultAudioSink.setOffloadMode(offloadMode)
    }

    override fun setOffloadDelayPadding(delayInFrames: Int, paddingInFrames: Int) {
        defaultAudioSink.setOffloadDelayPadding(delayInFrames, paddingInFrames)
    }

    override fun setAudioOutputProvider(audioOutputProvider: AudioOutputProvider) {
        defaultAudioSink.setAudioOutputProvider(audioOutputProvider)
    }

    override fun flush() = try {
        defaultAudioSink.flush()
    } finally {
        onAudioTrackUpdate(null)
    }

    override fun release() {
        defaultAudioSink.release()
    }
}
