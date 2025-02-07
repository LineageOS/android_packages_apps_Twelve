/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.audiobookshelf.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class LibraryMedia(
    @SerialName("metadata") val metadata: LibraryMetadata,
    @SerialName("coverPath") val coverPath: String,
    @SerialName("tags") val tags: List<String>,
    @SerialName("numTracks") val numTracks: Int,
    @SerialName("numAudioFiles") val numAudioFiles: Int,
    @SerialName("numChapters") val numChapters: Int,
    @SerialName("duration") val duration: Double,
    @SerialName("size") val size: Long,
    @SerialName("ebookFileFormat") val ebookFileFormat: String
)
