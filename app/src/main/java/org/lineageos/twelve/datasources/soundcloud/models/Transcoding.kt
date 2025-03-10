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
 * Transcoding info.
 *
 * @param url The URL to the stream
 * @param preset The preset
 * @param duration The duration
 * @param snipped Whether the stream is snipped
 * @param format The [Format]
 * @param quality The quality
 * @param isLegacyTranscoding Whether the stream is a legacy transcoding
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Transcoding(
    val url: Uri,
    val preset: String,
    val duration: Int, // TODO: ms?
    val snipped: Boolean,
    val format: Format,
    val quality: String,
    @SerialName("is_legacy_transcoding") val isLegacyTranscoding: Boolean,
) {
    /**
     * [Transcoding]'s format.
     *
     * @param protocol The protocol used in the stream
     * @param mimeType The MIME type of the stream
     */
    @Suppress("PROVIDED_RUNTIME_TOO_LOW")
    @Serializable
    data class Format(
        val protocol: String,
        @SerialName("mime_type") val mimeType: String,
    )
}
