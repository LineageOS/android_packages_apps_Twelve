/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.net.Uri
import androidx.media3.common.MediaMetadata
import org.lineageos.twelve.ext.buildMediaItem

/**
 * A user-defined playlist.
 *
 * @param uri The URI of the playlist
 * @param name The name of the playlist
 */
data class Playlist(
    override val uri: Uri,
    val name: String,
) : MediaItem<Playlist> {
    override val mediaType = MediaType.PLAYLIST

    override fun areContentsTheSame(other: Playlist) = compareValuesBy(
        this,
        other,
        Playlist::name,
    ) == 0

    override fun toMedia3MediaItem() = buildMediaItem(
        title = name,
        mediaId = "$PLAYLIST_MEDIA_ITEM_ID_PREFIX${uri}",
        isPlayable = false,
        isBrowsable = true,
        mediaType = MediaMetadata.MEDIA_TYPE_PLAYLIST,
        sourceUri = uri,
    )

    companion object {
        const val PLAYLIST_MEDIA_ITEM_ID_PREFIX = "[playlist]"
    }
}
