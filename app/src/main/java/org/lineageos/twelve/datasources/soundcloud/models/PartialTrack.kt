/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Partial [Track].
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class PartialTrack(
    val id: Int,
    val kind: Kind,
    @SerialName("monetization_model") val monetizationModel: String,
    val policy: String,
)
