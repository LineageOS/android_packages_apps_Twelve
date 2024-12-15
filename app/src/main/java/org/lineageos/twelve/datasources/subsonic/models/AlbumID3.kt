/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class AlbumID3(
    val id: String,
    val name: String,
    val artist: String? = null,
    val artistId: String? = null,
    val coverArt: String? = null,
    val songCount: Int,
    val duration: Int,
    val playCount: Long? = null,
    val created: InstantAsString,
    val starred: InstantAsString? = null,
    val year: Int? = null,
    val genre: String? = null,

    // OpenSubsonic
    val played: InstantAsString? = null,
    val sortName: String? = null,
    val userRating: UserRating? = null,

    // Navidrome
    val album: String? = null,
    val isDir: Boolean? = null,
    val isVideo: Boolean? = null,
    val parent: String? = null,
    val title: String? = null,
)
