/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * Lyrics.
 *
 * @param lyrics A list of lyric lines with their start time and text.
 */
data class Lyrics(
    val lyrics: List<LyricLine>
) {
    /**
     * Represents a single lyric line with a start time and the lyric text.
     */
    data class LyricLine(
        val start: Long, // Start time in milliseconds
        val line: String // The actual lyric text
    )
}
