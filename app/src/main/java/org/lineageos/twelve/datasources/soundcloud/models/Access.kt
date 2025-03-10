/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Access(val value: String) {
    @SerialName("playable")
    PLAYABLE("playable"),
    @SerialName("preview")
    PREVIEW("preview"),
    @SerialName("blocked")
    BLOCKED("blocked"),
}
