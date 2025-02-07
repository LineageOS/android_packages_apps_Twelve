/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.audiobookshelf.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class AudioMetaTags(
    @SerialName("tagAlbum") val tagAlbum: String,
    @SerialName("tagArtist") val tagArtist: String,
    @SerialName("tagGenre") val tagGenre: String,
    @SerialName("tagTitle") val tagTitle: String,
    @SerialName("tagTrack") val tagTrack: String,
    @SerialName("tagAlbumArtist") val tagAlbumArtist: String,
    @SerialName("tagComposer") val tagComposer: String,
)
