/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.models.OutputConfiguration
import org.lineageos.twelve.ui.views.ListItem
import org.lineageos.twelve.viewmodels.NowPlayingStatsViewModel
import java.util.Locale

/**
 * A fragment showing playback statistics for nerds and audiophiles thinking that audio files
 * with a sample rate higher than 48 kHz is better.
 */
class NowPlayingStatsBottomSheetDialogFragment : BottomSheetDialogFragment(
    R.layout.fragment_now_playing_stats_bottom_sheet_dialog
) {
    // View models
    private val viewModel by viewModels<NowPlayingStatsViewModel>()

    // Views
    private val deviceInfoImageView by getViewProperty<ImageView>(R.id.deviceInfoImageView)
    private val deviceMaxChannelCountListItem by getViewProperty<ListItem>(R.id.deviceMaxChannelCountListItem)
    private val deviceTypeListItem by getViewProperty<ListItem>(R.id.deviceTypeListItem)
    private val deviceHeaderListItem by getViewProperty<ListItem>(R.id.deviceHeaderListItem)
    private val deviceMaxSampleRateListItem by getViewProperty<ListItem>(R.id.deviceMaxSampleRateListItem)
    private val sourceChannelCountListItem by getViewProperty<ListItem>(R.id.sourceChannelCountListItem)
    private val sourceEncodingListItem by getViewProperty<ListItem>(R.id.sourceEncodingListItem)
    private val sourceFileTypeListItem by getViewProperty<ListItem>(R.id.sourceFileTypeListItem)
    private val sourceSampleRateListItem by getViewProperty<ListItem>(R.id.sourceSampleRateListItem)
    private val transcodingBitrateListItem by getViewProperty<ListItem>(R.id.transcodingBitrateListItem)
    private val transcodingEncodingListItem by getViewProperty<ListItem>(R.id.transcodingEncodingListItem)
    private val transcodingInfoImageView by getViewProperty<ImageView>(R.id.transcodingInfoImageView)
    private val transcodingOutputModeListItem by getViewProperty<ListItem>(R.id.transcodingOutputModeListItem)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.outputConfiguration.collectLatest { outputConfiguration ->
                        // Source
                        outputConfiguration?.source?.mimeType?.let { mimeType ->
                            sourceFileTypeListItem.supportingText = mimeType
                        } ?: sourceFileTypeListItem.setSupportingText(
                            R.string.audio_file_type_unknown
                        )

                        outputConfiguration?.source?.sampleRate?.also { sampleRate ->
                            sourceSampleRateListItem.setSupportingText(
                                R.string.audio_sample_rate_format,
                                decimalFormatter.format(sampleRate.toFloat() / 1000)
                            )
                        } ?: sourceSampleRateListItem.setSupportingText(
                            R.string.audio_sample_rate_unknown
                        )

                        outputConfiguration?.source?.channelCount?.let { channelCount ->
                            sourceChannelCountListItem.supportingText = channelCount.toString()
                        } ?: sourceChannelCountListItem.setSupportingText(
                            R.string.audio_channel_count_unknown
                        )

                        outputConfiguration?.source?.encoding?.also { encoding ->
                            sourceEncodingListItem.supportingText = encoding.displayName
                        } ?: sourceEncodingListItem.setSupportingText(
                            R.string.audio_encoding_unknown
                        )

                        // Transcoding configuration
                        transcodingInfoImageView.setImageResource(
                            when (outputConfiguration?.transcoding?.outputMode) {
                                OutputConfiguration.Transcoding.OutputMode.PCM ->
                                    R.drawable.ic_conversion_path

                                OutputConfiguration.Transcoding.OutputMode.OFFLOAD ->
                                    R.drawable.ic_conversion_path_off

                                OutputConfiguration.Transcoding.OutputMode.PASSTHROUGH ->
                                    R.drawable.ic_double_arrow

                                null -> R.drawable.ic_conversion_path
                            }
                        )

                        outputConfiguration?.transcoding?.encoding?.let { encoding ->
                            transcodingEncodingListItem.supportingText = encoding.displayName
                        } ?: transcodingEncodingListItem.setSupportingText(
                            R.string.audio_encoding_unknown
                        )

                        transcodingOutputModeListItem.setSupportingText(
                            when (outputConfiguration?.transcoding?.outputMode) {
                                OutputConfiguration.Transcoding.OutputMode.PCM ->
                                    R.string.audio_output_mode_pcm

                                OutputConfiguration.Transcoding.OutputMode.OFFLOAD ->
                                    R.string.audio_output_mode_offload

                                OutputConfiguration.Transcoding.OutputMode.PASSTHROUGH ->
                                    R.string.audio_output_mode_passthrough

                                null -> R.string.audio_output_mode_unknown
                            }
                        )

                        outputConfiguration?.transcoding?.bitrate?.also {
                            transcodingBitrateListItem.setSupportingText(
                                R.string.audio_bitrate_format,
                                decimalFormatter.format(it.toFloat() / 1000)
                            )
                            transcodingBitrateListItem.isVisible = true
                        } ?: run {
                            transcodingBitrateListItem.isVisible = false
                        }

                        // Output device
                        val outputDeviceDrawableResId = when (
                            outputConfiguration?.outputDevice?.type
                        ) {
                            OutputConfiguration.Device.Type.DEVICE ->
                                R.drawable.ic_mobile_speaker

                            OutputConfiguration.Device.Type.HEADPHONES ->
                                R.drawable.ic_headphones

                            OutputConfiguration.Device.Type.EXTERNAL_SPEAKERS ->
                                R.drawable.ic_speaker_group

                            OutputConfiguration.Device.Type.BLUETOOTH ->
                                R.drawable.ic_bluetooth

                            OutputConfiguration.Device.Type.HDMI ->
                                R.drawable.ic_settings_input_hdmi

                            OutputConfiguration.Device.Type.USB ->
                                R.drawable.ic_usb

                            OutputConfiguration.Device.Type.REMOTE ->
                                R.drawable.ic_cast

                            OutputConfiguration.Device.Type.HEARING_AID ->
                                R.drawable.ic_hearing_aid

                            null -> R.drawable.ic_media_output
                        }

                        deviceInfoImageView.setImageResource(outputDeviceDrawableResId)
                        deviceHeaderListItem.setLeadingIconImage(outputDeviceDrawableResId)

                        outputConfiguration?.outputDevice?.name?.also { name ->
                            deviceHeaderListItem.headlineText = name
                        } ?: deviceHeaderListItem.setHeadlineText(
                            R.string.audio_output
                        )

                        deviceTypeListItem.setSupportingText(
                            when (outputConfiguration?.outputDevice?.type) {
                                OutputConfiguration.Device.Type.DEVICE ->
                                    R.string.audio_output_device_type_device

                                OutputConfiguration.Device.Type.HEADPHONES ->
                                    R.string.audio_output_device_type_headphones

                                OutputConfiguration.Device.Type.EXTERNAL_SPEAKERS ->
                                    R.string.audio_output_device_type_external_speakers

                                OutputConfiguration.Device.Type.BLUETOOTH ->
                                    R.string.audio_output_device_type_bluetooth

                                OutputConfiguration.Device.Type.HDMI ->
                                    R.string.audio_output_device_type_hdmi

                                OutputConfiguration.Device.Type.USB ->
                                    R.string.audio_output_device_type_usb

                                OutputConfiguration.Device.Type.REMOTE ->
                                    R.string.audio_output_device_type_remote

                                OutputConfiguration.Device.Type.HEARING_AID ->
                                    R.string.audio_output_device_type_hearing_aid

                                null -> R.string.audio_output_device_type_unknown
                            }
                        )

                        outputConfiguration?.outputDevice?.sampleRates?.maxOrNull()
                            ?.also { sampleRate ->
                                deviceMaxSampleRateListItem.setSupportingText(
                                    R.string.audio_sample_rate_format,
                                    decimalFormatter.format(sampleRate.toFloat() / 1000)
                                )
                                deviceMaxSampleRateListItem.isVisible = true
                            } ?: run {
                            deviceMaxSampleRateListItem.isVisible = false
                        }

                        outputConfiguration?.outputDevice?.channelCounts?.maxOrNull()
                            ?.also { channelCount ->
                                deviceMaxChannelCountListItem.supportingText = "$channelCount"
                                deviceMaxChannelCountListItem.isVisible = true
                            } ?: run {
                            deviceMaxChannelCountListItem.isVisible = false
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val decimalFormatSymbols = DecimalFormatSymbols(Locale.ROOT)

        private val decimalFormatter = DecimalFormat("0.#", decimalFormatSymbols)
    }
}
