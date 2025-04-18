/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.media.AudioFormat
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
    val source: Source,

    /**
     * Information related to how the speaker will receive the audio.
     */
    val transcoding: Transcoding,

    /**
     * Output speaker configuration.
     */
    val outputDevice: Device,
) {
    data class Source(
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
             * Uncompressed lossless audio format.
             */
            UNCOMPRESSED,
        }
    }

    /**
     * Information related to how the [Device] will receive the [Source].
     *
     * @param outputMode The [OutputMode] of the transcoding
     * @param encoding The [Encoding] of the resulting stream
     * @param bitrate The bitrate of the resulting stream, may be null in certain configurations
     */
    data class Transcoding(
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

    /**
     * Information related to an output device.
     *
     * @param name User-friendly name of the device
     * @param type The [Type] of the device
     * @param sampleRates The sample rates supported by the device
     * @param channelCounts The supported number of channels configurations of the device
     * @param encodings A set of supported [Encoding]s
     */
    data class Device(
        val name: String?,
        val type: Type?,
        val sampleRates: Set<Int>,
        val channelCounts: Set<Int>,
        val encodings: Set<Encoding>,
    ) {
        enum class Type {
            /**
             * Internal speakers.
             */
            DEVICE,

            /**
             * Headphones connected to the device.
             */
            HEADPHONES,

            /**
             * External speakers.
             */
            EXTERNAL_SPEAKERS,

            /**
             * Bluetooth audio device.
             */
            BLUETOOTH,

            /**
             * HDMI audio device.
             */
            HDMI,

            /**
             * USB device output.
             */
            USB,

            /**
             * Remote audio device (cast).
             */
            REMOTE,

            /**
             * Hearing aid device.
             */
            HEARING_AID,
        }
    }

    /**
     * Audio stream encoding formats.
     */
    enum class Encoding(val displayName: String) {
        AAC_LC("Advanced Audio Coding Low Complexity (AAC-LC)"),
        AAC_HE_V1("Advanced Audio Coding High-Efficiency v1 (AAC HE v1)"),
        AAC_HE_V2("Advanced Audio Coding High-Efficiency v2 (AAC HE v2)"),
        AAC_XHE("Advanced Audio Coding Extended High-Efficiency (AAC xHE)"),
        AAC_ELD("Advanced Audio Coding Enhanced Low Delay (AAC ELD)"),
        AAC_ER_BSAC("Advanced Audio Coding Error Resilient Bit-Sliced Arithmetic Coding"),
        AC3("Dolby Digital (AC-3)"),
        AC4("Dolby Audio Codec 4 (AC-4)"),
        DOLBY_MAT("Dolby Metadata-enhanced Audio Transmission"),
        DOLBY_TRUEHD("Dolby TrueHD"),
        DRA("Dynamic Resolution Adaptation"),
        DSD("Direct Stream Digital"),
        DTS("DTS"),
        DTS_HD("DTS HD"),
        DTS_HD_MA("DTS HD Master Audio"),
        DTS_UHD_P1("DTS:X Profile-1"),
        DTS_UHD_P2("DTS:X Profile-2"),
        E_AC3("Dolby Digital Plus (E-AC-3)"),
        E_AC3_JOC("Dolby Digital Plus with Dolby Atmos (E-AC-3-JOC)"),
        IEC61937("IEC 61937"),
        MP3("MP3"),
        MPEGH_BL_L3("MPEG-H 3D Audio Baseline Profile (level 3)"),
        MPEGH_BL_L4("MPEG-H 3D Audio Baseline Profile (level 4)"),
        MPEGH_LC_L3("MPEG-H 3D Audio Low Complexity Profile (level 3)"),
        MPEGH_LC_L4("MPEG-H 3D Audio Low Complexity Profile (level 4)"),
        OPUS("Opus"),
        PCM_8BIT("PCM 8-bit"),
        PCM_16_BIT("PCM 16-bit"),
        PCM_24_BIT("PCM 24-bit"),
        PCM_24_BIT_PACKED("PCM 24-bit packed"),
        PCM_32_BIT("PCM 32-bit"),
        PCM_FLOAT("PCM 32-bit floating point");

        companion object {
            @androidx.annotation.OptIn(UnstableApi::class)
            fun fromMedia3Encoding(media3Encoding: @C.Encoding Int) = when (media3Encoding) {
                Format.NO_VALUE, C.ENCODING_INVALID -> null

                C.ENCODING_PCM_8BIT -> PCM_8BIT
                C.ENCODING_PCM_16BIT,
                C.ENCODING_PCM_16BIT_BIG_ENDIAN -> PCM_16_BIT
                C.ENCODING_PCM_24BIT,
                C.ENCODING_PCM_24BIT_BIG_ENDIAN -> PCM_24_BIT
                C.ENCODING_PCM_32BIT,
                C.ENCODING_PCM_32BIT_BIG_ENDIAN -> PCM_32_BIT
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

                else -> error("Unknown encoding: $media3Encoding")
            }

            fun fromAudioFormatEncoding(audioFormatEncoding: Int) = when (audioFormatEncoding) {
                AudioFormat.ENCODING_INVALID,
                AudioFormat.ENCODING_DEFAULT -> null

                AudioFormat.ENCODING_PCM_16BIT -> PCM_16_BIT
                AudioFormat.ENCODING_PCM_8BIT -> PCM_8BIT
                AudioFormat.ENCODING_PCM_FLOAT -> PCM_FLOAT
                AudioFormat.ENCODING_AC3 -> AC3
                AudioFormat.ENCODING_E_AC3 -> E_AC3
                AudioFormat.ENCODING_DTS -> DTS
                AudioFormat.ENCODING_DTS_HD -> DTS_HD
                AudioFormat.ENCODING_MP3 -> MP3
                AudioFormat.ENCODING_AAC_LC -> AAC_LC
                AudioFormat.ENCODING_AAC_HE_V1 -> AAC_HE_V1
                AudioFormat.ENCODING_AAC_HE_V2 -> AAC_HE_V2
                AudioFormat.ENCODING_IEC61937 -> IEC61937
                AudioFormat.ENCODING_DOLBY_TRUEHD -> DOLBY_TRUEHD
                AudioFormat.ENCODING_AAC_ELD -> AAC_ELD
                AudioFormat.ENCODING_AAC_XHE -> AAC_XHE
                AudioFormat.ENCODING_AC4 -> AC4
                AudioFormat.ENCODING_E_AC3_JOC -> E_AC3_JOC
                AudioFormat.ENCODING_DOLBY_MAT -> DOLBY_MAT
                AudioFormat.ENCODING_OPUS -> OPUS
                AudioFormat.ENCODING_PCM_24BIT_PACKED -> PCM_24_BIT_PACKED
                AudioFormat.ENCODING_PCM_32BIT -> PCM_32_BIT
                AudioFormat.ENCODING_MPEGH_BL_L3 -> MPEGH_BL_L3
                AudioFormat.ENCODING_MPEGH_BL_L4 -> MPEGH_BL_L4
                AudioFormat.ENCODING_MPEGH_LC_L3 -> MPEGH_LC_L3
                AudioFormat.ENCODING_MPEGH_LC_L4 -> MPEGH_LC_L4
                //AudioFormat.ENCODING_DTS_UHD -> DTS_UHD_P1 // ENCODING_DTS_UHD_P1
                AudioFormat.ENCODING_DRA -> DRA
                AudioFormat.ENCODING_DTS_HD_MA -> DTS_HD_MA
                AudioFormat.ENCODING_DTS_UHD_P1 -> DTS_UHD_P1
                AudioFormat.ENCODING_DTS_UHD_P2 -> DTS_UHD_P2
                AudioFormat.ENCODING_DSD -> DSD

                else -> error("Unknown encoding: $audioFormatEncoding")
            }
        }
    }
}
