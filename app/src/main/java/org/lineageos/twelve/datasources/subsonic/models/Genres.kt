/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

@Suppress("Provided_Runtime_Too_Low")
@Serializable
data class Genres(
    val genre: List<Genre>,
)
