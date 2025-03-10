/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data kind.
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
enum class Kind(val value: String) {
    /**
     * A playlist.
     * @see Playlist
     */
    @SerialName("playlist")
    PLAYLIST("playlist"),

    /**
     * A selection
     * @see Selection
     */
    @SerialName("selection")
    SELECTION("selection"),

    /**
     * A system playlist.
     * @see SystemPlaylist
     */
    @SerialName("system-playlist")
    SYSTEM_PLAYLIST("system-playlist"),

    /**
     * A track.
     * @see Track
     */
    @SerialName("track")
    TRACK("track"),

    /**
     * A user
     * @see User
     */
    @SerialName("user")
    USER("user");

    companion object {
        fun fromValue(value: String) = entries.firstOrNull { it.value == value }
    }
}
