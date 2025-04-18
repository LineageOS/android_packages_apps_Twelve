/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.repositories

import android.media.AudioDeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.lineageos.twelve.models.OutputConfiguration

/**
 * Repository holding current output configuration info.
 */
class OutputConfigurationRepository {
    private val _device =
        MutableStateFlow<OutputConfiguration.Device?>(null)
    val device = _device.asStateFlow()

    fun updateAudioDeviceInfo(audioDeviceInfo: AudioDeviceInfo?) {
        _device.value = audioDeviceInfo?.toModel()
    }

    private fun AudioDeviceInfo.toModel() = OutputConfiguration.Device(
        name = productName.toString(),
        type = when (type) {
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE,
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE ->
                OutputConfiguration.Device.Type.DEVICE

            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES ->
                OutputConfiguration.Device.Type.HEADPHONES

            AudioDeviceInfo.TYPE_LINE_ANALOG,
            AudioDeviceInfo.TYPE_LINE_DIGITAL,
            AudioDeviceInfo.TYPE_DOCK,
            AudioDeviceInfo.TYPE_AUX_LINE,
            AudioDeviceInfo.TYPE_BUS,
            AudioDeviceInfo.TYPE_DOCK_ANALOG ->
                OutputConfiguration.Device.Type.EXTERNAL_SPEAKERS

            AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_BLE_SPEAKER,
            AudioDeviceInfo.TYPE_BLE_BROADCAST ->
                OutputConfiguration.Device.Type.BLUETOOTH

            AudioDeviceInfo.TYPE_HDMI,
            AudioDeviceInfo.TYPE_HDMI_ARC,
            AudioDeviceInfo.TYPE_HDMI_EARC ->
                OutputConfiguration.Device.Type.HDMI

            AudioDeviceInfo.TYPE_USB_DEVICE,
            AudioDeviceInfo.TYPE_USB_ACCESSORY,
            AudioDeviceInfo.TYPE_USB_HEADSET ->
                OutputConfiguration.Device.Type.USB

            AudioDeviceInfo.TYPE_IP,
            AudioDeviceInfo.TYPE_REMOTE_SUBMIX ->
                OutputConfiguration.Device.Type.REMOTE

            AudioDeviceInfo.TYPE_HEARING_AID ->
                OutputConfiguration.Device.Type.HEARING_AID

            else -> null
        },
        sampleRates = sampleRates.toSet(),
        channelCounts = channelCounts.toSet(),
        encodings = encodings.toList().mapNotNull {
            OutputConfiguration.Encoding.fromAudioFormatEncoding(it)
        }.toSet(),
    )
}
