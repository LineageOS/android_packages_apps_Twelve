/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.graphics.Bitmap
import android.net.Uri
import androidx.media3.common.MediaMetadata
import org.lineageos.twelve.ext.buildMediaItem

/**
 * An album.
 *
 * @param uri The URI of the album
 * @param title The title of the album
 * @param artistUri The URI of the artist
 * @param artistName The name of the artist
 * @param year The year of the album
 * @param thumbnail The album's thumbnail
 */
data class Album(
    val uri: Uri,
    val title: String,
    val artistUri: Uri,
    val artistName: String,
    val year: Int?,
    val thumbnail: Bitmap?,
) : UniqueItem<Album> {
    override fun areItemsTheSame(other: Album) = this.uri == other.uri

    override fun areContentsTheSame(other: Album) = compareValuesBy(
        this, other,
        Album::title,
        Album::artistUri,
        Album::artistName,
        Album::year,
        { it.thumbnail?.sameAs(other.thumbnail) ?: (other.thumbnail == null) },
    ) == 0

    override fun toMediaItem() = buildMediaItem(
        title = title,
        mediaId = "${ALBUM_MEDIA_ITEM_ID_PREFIX}${uri}",
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_ALBUM,
        sourceUri = uri,
    )

    companion object {
        const val ALBUM_MEDIA_ITEM_ID_PREFIX = "[album]"
    }
}
