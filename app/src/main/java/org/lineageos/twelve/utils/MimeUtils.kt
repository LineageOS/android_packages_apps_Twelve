/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import org.lineageos.twelve.datasources.MediaType

object MimeUtils {
    @androidx.annotation.OptIn(UnstableApi::class)
    fun mimeTypeToDisplayName(mimeType: String) = MimeTypes.normalizeMimeType(mimeType).let {
        it.takeIf { it.contains('/') }
            ?.substringAfterLast('/')
            ?.uppercase()
    }

    fun mimeTypeToMediaType(mimeType: String) = when {
        mimeType.startsWith("audio/") -> MediaType.AUDIO

        else -> when (mimeType) {
            "application/itunes",
            "application/ogg",
            "application/x-ogg",
            "vnd.android.cursor.item/audio" -> MediaType.AUDIO

            "vnd.android.cursor.item/album" -> MediaType.ALBUM

            "vnd.android.cursor.item/artist" -> MediaType.ARTIST

            "vnd.android.cursor.item/playlist" -> MediaType.PLAYLIST

            else -> null
        }
    }
}
