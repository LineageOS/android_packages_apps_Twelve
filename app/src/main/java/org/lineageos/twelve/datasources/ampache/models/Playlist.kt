/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Playlist.
 *
 * @param id The ID
 * @param name The name
 * @param owner The owner
 * @param user The user
 * @param items The items
 * @param type The type
 * @param art The artwork URL
 * @param hasAccess Whether the user has access
 * @param hasCollaborate Whether the user has collaborate
 * @param hasArt Whether this playlist has an artwork available
 * @param flag The flag
 * @param rating The rating
 * @param averageRating The average rating
 * @param md5 The md5
 * @param lastUpdate The last update
 */
@Serializable
data class Playlist(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("owner") val owner: String,
    @SerialName("user") val user: User? = null,
    @SerialName("items") val items: Int,
    @SerialName("type") val type: String,
    @SerialName("art") val art: String,
    @SerialName("has_access") val hasAccess: Boolean,
    @SerialName("has_collaborate") val hasCollaborate: Boolean,
    @SerialName("has_art") val hasArt: Boolean,
    @SerialName("flag") val flag: Boolean,
    @SerialName("rating") val rating: Int?,
    @SerialName("averagerating") val averageRating: Double? = null,
    @SerialName("md5") val md5: String?,
    @SerialName("last_update") val lastUpdate: InstantAsTimestampLong,
)
