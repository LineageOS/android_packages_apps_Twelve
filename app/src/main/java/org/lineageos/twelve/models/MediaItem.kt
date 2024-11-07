/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.net.Uri

/**
 * A media item.
 */
sealed interface MediaItem<T : MediaItem<T>> : UniqueItem<T> {
    /**
     * The media type.
     */
    val mediaType: MediaType

    /**
     * A [Uri] identifying this media item.
     */
    val uri: Uri

    override fun areItemsTheSame(other: T) = this.uri == other.uri

    /**
     * Convert this item to a media item.
     */
    fun toMedia3MediaItem(): androidx.media3.common.MediaItem
}
