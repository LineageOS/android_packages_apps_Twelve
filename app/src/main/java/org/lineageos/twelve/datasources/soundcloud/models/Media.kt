/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud.models

import kotlinx.serialization.Serializable

/**
 * Media info.
 *
 * @param transcodings List of [Transcoding]
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Media(
    val transcodings: List<Transcoding>,
)
