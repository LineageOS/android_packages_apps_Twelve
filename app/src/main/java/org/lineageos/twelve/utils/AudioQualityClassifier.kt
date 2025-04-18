/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.DefaultAudioSink
import org.lineageos.twelve.models.OutputConfiguration

/**
 * Audio quality classifier.
 */
@androidx.annotation.OptIn(UnstableApi::class)
object AudioQualityClassifier {
    private val mimeTypeToFormatType = mapOf(
        MimeTypes.AUDIO_MP4 to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_AAC to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_MATROSKA to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_WEBM to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_MPEG to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_MPEG_L1 to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_MPEG_L2 to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_MPEGH_MHA1 to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_MPEGH_MHM1 to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_RAW to OutputConfiguration.SourceInformation.FormatType.UNCOMPRESSED,
        MimeTypes.AUDIO_ALAW to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_MLAW to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_AC3 to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_E_AC3 to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_E_AC3_JOC to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_AC4 to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_TRUEHD to OutputConfiguration.SourceInformation.FormatType.LOSSLESS,
        MimeTypes.AUDIO_DTS to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_DTS_HD to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_DTS_EXPRESS to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_DTS_X to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_VORBIS to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_OPUS to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_AMR to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_AMR_NB to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_AMR_WB to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_FLAC to OutputConfiguration.SourceInformation.FormatType.LOSSLESS,
        MimeTypes.AUDIO_ALAC to OutputConfiguration.SourceInformation.FormatType.LOSSLESS,
        MimeTypes.AUDIO_MSGSM to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_OGG to OutputConfiguration.SourceInformation.FormatType.LOSSY,
        MimeTypes.AUDIO_WAV to OutputConfiguration.SourceInformation.FormatType.UNCOMPRESSED,
        // ...sort of
        MimeTypes.AUDIO_MIDI to OutputConfiguration.SourceInformation.FormatType.LOSSLESS,
        // This format allows both, this is a sane guess
        MimeTypes.AUDIO_IAMF to OutputConfiguration.SourceInformation.FormatType.LOSSLESS,
    )

    fun classify(
        sourceFormat: Format?,
        transcodingEncoding: @C.Encoding Int?,
        transcodingOutputMode: @DefaultAudioSink.OutputMode Int?,
        transcodingBitrate: Int?,
        audioDeviceConfiguration: OutputConfiguration.AudioDeviceConfiguration?,
    ): OutputConfiguration? {
        val sourceInformation = sourceFormat?.let {
            val mimeType = it.sampleMimeType ?: it.containerMimeType

            OutputConfiguration.SourceInformation(
                sampleRate = it.sampleRate,
                channelCount = it.channelCount,
                mimeType = mimeType,
                formatType = mimeTypeToFormatType[mimeType],
                encoding = it.guessEncoding(),
            )
        } ?: return null

        val transcodingConfiguration = run {
            OutputConfiguration.TranscodingConfiguration(
                outputMode = transcodingOutputMode?.let {
                    OutputConfiguration.TranscodingConfiguration.OutputMode.fromMedia3OutputMode(
                        it
                    )
                } ?: return@run null,
                encoding = transcodingEncoding?.let {
                    OutputConfiguration.Encoding.fromMedia3Encoding(it)
                } ?: return@run null,
                bitrate = transcodingBitrate,
            )
        } ?: return null

        return OutputConfiguration(
            sourceInformation = sourceInformation,
            transcodingConfiguration = transcodingConfiguration,
            audioDeviceConfiguration = audioDeviceConfiguration ?: return null,
        )
    }

    private fun Format.guessEncoding() = sampleMimeType?.let {
        OutputConfiguration.Encoding.fromMedia3Encoding(
            MimeTypes.getEncoding(
                MimeTypes.normalizeMimeType(it),
                codecs,
            )
        )
    } ?: OutputConfiguration.Encoding.fromMedia3Encoding(pcmEncoding)
}
