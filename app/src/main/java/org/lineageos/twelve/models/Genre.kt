/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.net.Uri
import androidx.media3.common.MediaMetadata
import org.lineageos.twelve.ext.buildMediaItem

/**
 * A music genre.
 * TODO: Maybe make it an enum class and follow https://en.wikipedia.org/wiki/List_of_ID3v1_genres
 *
 * @param uri The URI of the genre
 * @param name The name of the genre. Can be null
 */
data class Genre(
    override val uri: Uri,
    val name: String?,
) : MediaItem<Genre> {
    override val mediaType = MediaType.GENRE

    override fun areContentsTheSame(other: Genre) = compareValuesBy(
        other,
        this,
        Genre::name,
    ) == 0

    override fun toMedia3MediaItem() = buildMediaItem(
        title = name ?: "Unknown",
        mediaId = "$GENRE_MEDIA_ITEM_ID_PREFIX${uri}",
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_GENRE,
        sourceUri = uri,
    )

    companion object {
        const val GENRE_MEDIA_ITEM_ID_PREFIX = "[genre]"
    }
}
