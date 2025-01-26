/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.jellyfin.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("Provided_Runtime_Too_Low")
@Serializable
data class AuthenticateUserResult(
    @SerialName("AccessToken") val accessToken: String? = null,
)
