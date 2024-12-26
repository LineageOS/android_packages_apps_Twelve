/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * Sorting strategies for media items.
 * All of those are ascending by default (e.g. A-Z or 0-n).
 */
enum class SortingStrategy {
    /**
     * Sort alphabetically by artist name.
     */
    ARTIST,

    /**
     * Sort by creation or release date, oldest to newest.
     */
    CREATION_DATE,

    /**
     * Sort by modification or update date, oldest to newest.
     */
    MODIFICATION_DATE,

    /**
     * Sort alphabetically by name or title.
     */
    NAME,

    /**
     * Sort by user's play count, least to most.
     */
    PLAY_COUNT,

    ;

    companion object {

        fun fromName(str: String, fallback: SortingStrategy): SortingStrategy = when (str) {
            CREATION_DATE.name -> CREATION_DATE
            MODIFICATION_DATE.name -> MODIFICATION_DATE
            NAME.name -> NAME
            ARTIST.name -> ARTIST
            PLAY_COUNT.name -> PLAY_COUNT
            else -> fallback
        }
    }
}
