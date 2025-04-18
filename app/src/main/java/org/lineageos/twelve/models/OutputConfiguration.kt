/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.DefaultAudioSink

/**
 * Data class describing the whole output setup, from the source stream up to the speakers.
 */
data class OutputConfiguration(
    /**
     * The audio stream format information.
     */
    val sourceInformation: SourceInformation,

    /**
     * Information related to how the speaker will receive the audio.
     */
    val transcodingConfiguration: TranscodingConfiguration?,

    /**
     * Output speaker configuration.
     */
    val audioDeviceConfiguration: AudioDeviceConfiguration?,
) {
    data class SourceInformation(
        val sampleRate: Int,
        val channelCount: Int,
        val mimeType: String?,
        val formatType: FormatType?,
        val encoding: Encoding?,
    ) {
        /**
         * Audio format type.
         */
        enum class FormatType {
            /**
             * Lossy audio format.
             */
            LOSSY,

            /**
             * Compressed lossless audio format.
             */
            LOSSLESS,

            /**
             * Uncompressed audio format.
             */
            UNCOMPRESSED,
        }
    }

    data class TranscodingConfiguration(
        val outputMode: OutputMode,
        val encoding: Encoding,
        val bitrate: Int?,
    ) {
        /**
         * Audio output mode.
         */
        @androidx.annotation.OptIn(UnstableApi::class)
        enum class OutputMode {
            /**
             * The audio sink plays PCM audio.
             */
            PCM,

            /**
             * The audio sink plays encoded audio in offload.
             */
            OFFLOAD,

            /**
             * The audio sink plays encoded audio in passthrough.
             */
            PASSTHROUGH;

            companion object {
                fun fromMedia3OutputMode(
                    media3OutputMode: @DefaultAudioSink.OutputMode Int,
                ) = when (media3OutputMode) {
                    DefaultAudioSink.OUTPUT_MODE_PCM -> PCM
                    DefaultAudioSink.OUTPUT_MODE_OFFLOAD -> OFFLOAD
                    DefaultAudioSink.OUTPUT_MODE_PASSTHROUGH -> PASSTHROUGH
                    else -> error("Unknown output mode: $media3OutputMode")
                }
            }
        }
    }

    data class AudioDeviceConfiguration(
        val type: Type,
        val sampleRate: Int,
        val channelCount: Int,
    ) {
        enum class Type {
            /**
             * Internal speaker.
             */
            INTERNAL,

            /**
             * Headphones connected to the device.
             */
            HEADPHONES,

            /**
             * Bluetooth audio device.
             */
            BLUETOOTH,

            /**
             * HDMI audio device.
             */
            HDMI,

            /**
             * Remote audio device (cast).
             */
            REMOTE,
        }
    }

    /**
     * Audio encoding formats.
     */
    enum class Encoding(val displayName: String) {
        PCM_8BIT("PCM 8-bit"),
        PCM_16BIT("PCM 16-bit"),
        PCM_16_BIT_BIG_ENDIAN("PCM 16-bit (big endian)"),
        PCM_24BIT("PCM 24-bit"),
        PCM_24_BIT_BIG_ENDIAN("PCM 24-bit (big endian)"),
        PCM_32BIT("PCM 32-bit"),
        PCM_32_BIT_BIG_ENDIAN("PCM 32-bit (big endian)"),
        PCM_FLOAT("PCM 32-bit floating point"),
        MP3("MP3"),
        AAC_LC("Advanced Audio Coding Low Complexity (AAC-LC)"),
        AAC_HE_V1("Advanced Audio Coding High-Efficiency v1 (AAC HE v1)"),
        AAC_HE_V2("Advanced Audio Coding High-Efficiency v2 (AAC HE v2)"),
        AAC_XHE("Advanced Audio Coding Extended High-Efficiency (AAC xHE)"),
        AAC_ELD("Advanced Audio Coding Enhanced Low Delay (AAC ELD)"),
        AAC_ER_BSAC("Advanced Audio Coding Error Resilient Bit-Sliced Arithmetic Coding"),
        AC3("Dolby Digital (AC-3)"),
        E_AC3("Dolby Digital Plus (E-AC-3)"),
        E_AC3_JOC("Dolby Digital Plus with Dolby Atmos (E-AC-3-JOC)"),
        AC4("Dolby Audio Codec 4 (AC-4)"),
        DTS("DTS"),
        DTS_HD("DTS HD"),
        DOLBY_TRUEHD("Dolby TrueHD"),
        OPUS("Opus"),
        DTS_UHD_P2("DTS UHD Profile-2");

        companion object {
            @androidx.annotation.OptIn(UnstableApi::class)
            fun fromMedia3Encoding(media3Encoding: @C.Encoding Int) = when (media3Encoding) {
                C.ENCODING_PCM_8BIT -> PCM_8BIT
                C.ENCODING_PCM_16BIT -> PCM_16BIT
                C.ENCODING_PCM_16BIT_BIG_ENDIAN -> PCM_16_BIT_BIG_ENDIAN
                C.ENCODING_PCM_24BIT -> PCM_24BIT
                C.ENCODING_PCM_24BIT_BIG_ENDIAN -> PCM_24_BIT_BIG_ENDIAN
                C.ENCODING_PCM_32BIT -> PCM_32BIT
                C.ENCODING_PCM_32BIT_BIG_ENDIAN -> PCM_32_BIT_BIG_ENDIAN
                C.ENCODING_PCM_FLOAT -> PCM_FLOAT
                C.ENCODING_MP3 -> MP3
                C.ENCODING_AAC_LC -> AAC_LC
                C.ENCODING_AAC_HE_V1 -> AAC_HE_V1
                C.ENCODING_AAC_HE_V2 -> AAC_HE_V2
                C.ENCODING_AAC_XHE -> AAC_XHE
                C.ENCODING_AAC_ELD -> AAC_ELD
                C.ENCODING_AAC_ER_BSAC -> AAC_ER_BSAC
                C.ENCODING_AC3 -> AC3
                C.ENCODING_E_AC3 -> E_AC3
                C.ENCODING_E_AC3_JOC -> E_AC3_JOC
                C.ENCODING_AC4 -> AC4
                C.ENCODING_DTS -> DTS
                C.ENCODING_DTS_HD -> DTS_HD
                C.ENCODING_DOLBY_TRUEHD -> DOLBY_TRUEHD
                C.ENCODING_OPUS -> OPUS
                C.ENCODING_DTS_UHD_P2 -> DTS_UHD_P2

                Format.NO_VALUE, C.ENCODING_INVALID -> null
                else -> error("Unknown encoding: $media3Encoding")
            }
        }
    }
}
