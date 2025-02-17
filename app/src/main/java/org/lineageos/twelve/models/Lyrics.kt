/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * Lyrics.
 *
 * @param lines A list of [Line]
 */
data class Lyrics(
    val lines: List<Line>,
) {
    /**
     * Represents a single lyric line with the lyric text and the duration time.
     *
     * @param text The actual lyric text
     * @param durationMs A range that defines the start and end time of the line, in milliseconds.
     *   When only the start time is specified, it is assumed to be the same as the end time
     */
    data class Line(
        val text: String,
        val durationMs: LongRange?,
    )

    class Builder {
        private data class TempLine(
            val text: String,
            val startMs: Long?,
            val endMs: Long?,
        ) {
            fun toLine() = Line(
                text = text,
                durationMs = startMs?.let { it..(endMs ?: it) },
            )
        }

        private val lines = mutableListOf<TempLine>()

        /**
         * Add a new line.
         *
         * @param line The [Line]
         */
        fun addLine(line: Line) {
            lines.add(line.toTempLine())
        }

        /**
         * Add a new line. If necessary, end instant will be calculated from the other lines.
         *
         * @param text [Line.text]
         * @param startMs [Line.durationMs]
         * @param endMs [Line.durationMs]
         */
        fun addLine(
            text: String,
            startMs: Long? = null,
            endMs: Long? = null,
        ) {
            lines.add(
                TempLine(
                    text = text,
                    startMs = startMs,
                    endMs = endMs,
                )
            )
        }

        /**
         * Build the [Lyrics].
         */
        fun build() = Lyrics(
            lines = lines.sortedBy { it.startMs }.let { sortedLines ->
                val startDurations = sortedLines.mapNotNull { it.startMs }

                sortedLines.map { line ->
                    val endMs = line.endMs ?: line.startMs?.let { startMs ->
                        startDurations.firstNotNullOfOrNull { start ->
                            start.takeIf { startMs > it }
                        } ?: startMs
                    }

                    line.copy(endMs = endMs).toLine()
                }
            },
        )

        private fun Line.toTempLine() = TempLine(
            text = text,
            startMs = durationMs?.first,
            endMs = durationMs?.last,
        )
    }
}
