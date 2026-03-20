/*
 * SPDX-FileCopyrightText: 2025-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.services

import android.media.AudioDeviceInfo
import android.media.AudioRouting
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import androidx.media3.common.Format
import androidx.media3.common.util.Clock
import androidx.media3.exoplayer.analytics.PlayerId
import androidx.media3.exoplayer.audio.AudioOffloadSupport
import androidx.media3.exoplayer.audio.AudioOutputProvider
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.AudioTrackAudioOutput
import androidx.media3.exoplayer.audio.DefaultAudioSink

/**
 * An [AudioSink] implementation that wraps a [DefaultAudioSink] and exposes the routed
 * [AudioDeviceInfo] used by the [DefaultAudioSink].
 */
@androidx.media3.common.util.UnstableApi
class TwelveAudioSink(
    private val defaultAudioSink: DefaultAudioSink,
    private val onAudioDeviceInfoChanged: (AudioDeviceInfo?) -> Unit,
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

    private var externalListener: AudioSink.Listener? = null

    private val handler = Handler(Looper.getMainLooper())

    private val routingListener = AudioRouting.OnRoutingChangedListener { routing ->
        onAudioDeviceInfoChanged(routing.routedDevice)
    }

    private var currentAudioTrack: AudioTrack? = null

    init {
        defaultAudioSink.setListener(object : AudioSink.Listener {
            override fun onAudioTrackInitialized(audioTrackConfig: AudioSink.AudioTrackConfig) {
                currentAudioTrack?.removeOnRoutingChangedListener(routingListener)

                val track = audioTrack
                currentAudioTrack = track

                track?.addOnRoutingChangedListener(routingListener, handler)
                onAudioDeviceInfoChanged(track?.routedDevice)

                externalListener?.onAudioTrackInitialized(audioTrackConfig)
            }

            override fun onAudioTrackReleased(audioTrackConfig: AudioSink.AudioTrackConfig) {
                if (currentAudioTrack == null) {
                    onAudioDeviceInfoChanged(null)
                }

                externalListener?.onAudioTrackReleased(audioTrackConfig)
            }

            override fun onPositionDiscontinuity() {
                externalListener?.onPositionDiscontinuity()
            }

            override fun onUnderrun(p0: Int, p1: Long, p2: Long) {
                externalListener?.onUnderrun(p0, p1, p2)
            }

            override fun onSkipSilenceEnabledChanged(p0: Boolean) {
                externalListener?.onSkipSilenceEnabledChanged(p0)
            }
        })
    }

    override fun setListener(listener: AudioSink.Listener) {
        externalListener = listener
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

    override fun flush() {
        defaultAudioSink.flush()
    }

    override fun release() {
        audioTrack?.removeOnRoutingChangedListener(routingListener)
        defaultAudioSink.release()
    }
}
