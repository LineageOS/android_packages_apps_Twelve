/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.audiobookshelf.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class AudioFile(
    @SerialName("index") val index: Int,
    @SerialName("metadata") val metadata: AudioMetadata,
    @SerialName("metaTags") val audioMetaTags: AudioMetaTags
)
