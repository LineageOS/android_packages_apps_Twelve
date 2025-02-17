/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * A single lyric line.
 *
 * @param lines A list of [Line]
 */
data class Lyrics(
    val lines: List<Line>,
) {
    /**
     * Represents a single lyric line with a start time and the lyric text.
     *
     * @param line The actual lyric text
     * @param duration A range that defines when this line is relevant
     */
    data class Line(
        val line: String,
        val duration: LongRange,
    )
}
