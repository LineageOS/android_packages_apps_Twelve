/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.audiobookshelf.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class LibraryMetadata(
    @SerialName("title") val title: String,
    @SerialName("titleIgnorePrefix") val titleIgnorePrefix: String,
    @SerialName("subtitle") val subtitle: String?,
    @SerialName("authorName") val authorName: String,
    @SerialName("narratorName") val narratorName: String,
    @SerialName("seriesName") val seriesName: String,
    @SerialName("genres") val genres: List<String>,
    @SerialName("publishedYear") val publishedYear: String,
    @SerialName("publishedDate") val publishedDate: String?,
    @SerialName("publisher") val publisher: String,
    @SerialName("description") val description: String,
    @SerialName("isbn") val isbn: String?,
    @SerialName("asin") val asin: String,
    @SerialName("language") val language: String?,
    @SerialName("explicit") val explicit: Boolean
)
