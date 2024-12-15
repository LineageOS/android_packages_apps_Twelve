/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import kotlinx.serialization.Serializable

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class AlbumWithSongsID3(
    // AlbumID3 start

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
    val sortName: String? = null,

    // Navidrome
    val parent: String? = null,

    // AlbumID3 end

    val song: List<Child>,
) {
    fun toAlbumID3() = AlbumID3(
        id = id,
        name = name,
        artist = artist,
        artistId = artistId,
        coverArt = coverArt,
        songCount = songCount,
        duration = duration,
        playCount = playCount,
        created = created,
        starred = starred,
        year = year,
        genre = genre,
        sortName = sortName,
        parent = parent,
    )
}
