/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.audiobookshelf.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class AudioMetadata(
    @SerialName("filename") val filename: String,
    @SerialName("ext") val ext: String,
    @SerialName("path") val path: String,
    @SerialName("relPath") val relPath: String,
    @SerialName("size") val size: Long,
    @SerialName("mtimeMs") val mtimeMs: Long,
    @SerialName("ctimeMs") val ctimeMs: Long,
    @SerialName("birthtimeMs") val birthtimeMs: Long,
)
