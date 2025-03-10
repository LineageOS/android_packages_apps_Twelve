/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:UseSerializers(UriSerializer::class)

package org.lineageos.twelve.datasources.soundcloud.models

import android.net.Uri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.lineageos.twelve.datasources.soundcloud.serializers.UriSerializer

/**
 * Streaming endpoints.
 *
 * @param httpMp3128Url HTTP MP3 128kbps streaming URL
 * @param hlsMp3128Url HLS MP3 128kbps streaming URL
 * @param hlsOpus64Url HLS OPUS 64kbps streaming URL
 * @param previewMp3128Url Preview MP3 128kbps streaming URL
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Streams(
    @SerialName("http_mp3_128_url") val httpMp3128Url: Uri,
    @SerialName("hls_mp3_128_url") val hlsMp3128Url: Uri,
    @SerialName("hls_opus_64_url") val hlsOpus64Url: Uri,
    @SerialName("preview_mp3_128_url") val previewMp3128Url: Uri,
)
