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
     * Represents a single lyric line with the lyric text and the duration time.
     *
     * @param line The actual lyric text
     * @param durationMs A range that defines the start and end time of the line, in milliseconds.
     *   When only the start time is specified, it is assumed to be the same as the end time
     */
    data class Line(
        val line: String,
        val durationMs: LongRange?,
    )
}
