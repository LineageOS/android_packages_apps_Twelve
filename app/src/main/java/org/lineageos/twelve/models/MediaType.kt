/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Media types handled by the data sources.
 */
@Parcelize
enum class MediaType : Parcelable {
    ALBUM,
    ARTIST,
    AUDIO,
    GENRE,
    PLAYLIST,
}
