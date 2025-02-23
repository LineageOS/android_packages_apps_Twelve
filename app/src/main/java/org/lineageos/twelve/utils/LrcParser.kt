/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import android.util.Log
import org.lineageos.twelve.models.Lyrics

/**
 * https://en.wikipedia.org/wiki/LRC_(file_format)
 */
object LrcParser {
    private val LOG_TAG = LrcParser::class.simpleName!!

    private val LRC_TAG_VALUE_REGEX = """^\[(.*)](.*)$""".toRegex()

    private val LRC_TIME_REGEX = Regex("""^(\d{2}):(\d{2})\.(\d{2})$""")

    /**
     * LRC lyrics.
     *
     * @param ti Title of the song
     * @param ar Artist performing this song
     * @param al Album the song is from
     * @param au Author of the song
     * @param lr Lyricist of the song
     * @param lengthMs Length of the song in milliseconds
     * @param by Author of the LRC file (not the song)
     * @param offsetMs Specifies a global offset value for the lyric times, in milliseconds. The
     *   value is prefixed with either + or -, with + causing lyrics to appear sooner
     * @param re The player or editor that created the LRC file
     * @param ve The version of the program
     * @param lines The lyrics lines
     */
    data class Lrc(
        val ti: String?,
        val ar: String?,
        val al: String?,
        val au: String?,
        val lr: String?,
        val lengthMs: Long?,
        val by: String?,
        val offsetMs: Long?,
        val re: String?,
        val ve: String?,
        val lines: List<Line>,
    ) {
        data class Line(
            val timeMs: Long?,
            val text: String,
        )

        class Builder {
            private var ti: String? = null
            private var ar: String? = null
            private var al: String? = null
            private var au: String? = null
            private var lr: String? = null
            private var lengthMs: Long? = null
            private var by: String? = null
            private var offsetMs: Long? = null
            private var re: String? = null
            private var ve: String? = null
            private val lines = mutableListOf<Line>()

            /**
             * @see Lrc.ti
             */
            fun setTi(ti: String?) = apply { this.ti = ti }

            /**
             * @see Lrc.ar
             */
            fun setAr(ar: String?) = apply { this.ar = ar }

            /**
             * @see Lrc.al
             */
            fun setAl(al: String?) = apply { this.al = al }

            /**
             * @see Lrc.au
             */
            fun setAu(au: String?) = apply { this.au = au }

            /**
             * @see Lrc.lr
             */
            fun setLr(lr: String?) = apply { this.lr = lr }

            /**
             * @see Lrc.lengthMs
             */
            fun setLengthMs(lengthMs: Long?) = apply { this.lengthMs = lengthMs }

            /**
             * @see Lrc.by
             */
            fun setBy(by: String?) = apply { this.by = by }

            /**
             * @see Lrc.offsetMs
             */
            fun setOffsetMs(offsetMs: Long?) = apply { this.offsetMs = offsetMs }

            /**
             * @see Lrc.re
             */
            fun setRe(re: String?) = apply { this.re = re }

            /**
             * @see Lrc.ve
             */
            fun setVe(ve: String?) = apply { this.ve = ve }

            /**
             * @see Lrc.lines
             */
            fun addLine(line: Line) = apply { lines.add(line) }

            fun build() = when {
                listOfNotNull(
                    ti,
                    ar,
                    al,
                    au,
                    lr,
                    lengthMs,
                    by,
                    offsetMs,
                    re,
                    ve,
                ).isNotEmpty() && lines.isNotEmpty() -> Lrc(
                    ti = ti,
                    ar = ar,
                    al = al,
                    au = au,
                    lr = lr,
                    lengthMs = lengthMs,
                    by = by,
                    offsetMs = offsetMs,
                    re = re,
                    ve = ve,
                    lines = lines,
                )

                else -> null
            }
        }

        fun toLyrics() = Lyrics.Builder()
            .apply {
                for (line in lines) {
                    addLine(line.text, line.timeMs)
                }
            }
            .build()
    }

    private data class LrcTag<T>(
        private val name: String,
        private val property: Lrc.Builder.(T?) -> Lrc.Builder,
        private val parser: String.() -> T?
    ) {
        fun tryParseAndSet(builder: Lrc.Builder, tag: String): Boolean {
            val tagPrefix = "$name:"
            if (!tag.startsWith(tagPrefix)) {
                return false
            }

            val value = tag.removePrefix(tagPrefix).trim()
            property(builder, parser(value))

            return true
        }
    }

    private val allLrcTags = listOf(
        LrcTag("ti", Lrc.Builder::setTi, String::toString),
        LrcTag("ar", Lrc.Builder::setAr, String::toString),
        LrcTag("al", Lrc.Builder::setAl, String::toString),
        LrcTag("au", Lrc.Builder::setAu, String::toString),
        LrcTag("lr", Lrc.Builder::setLr, String::toString),
        LrcTag("length", Lrc.Builder::setLengthMs) {
            val (minutes, seconds) = split(":", limit = 2)
            (minutes.toLong() * 60 * 1000) + (seconds.toLong() * 1000)
        },
        LrcTag("by", Lrc.Builder::setBy, String::toString),
        LrcTag("offset", Lrc.Builder::setOffsetMs) {
            when {
                startsWith("+") -> substring(1).toLong()
                startsWith("-") -> substring(1).toLong() * -1
                else -> null
            }
        },
        LrcTag("re", Lrc.Builder::setRe, String::toString),
        LrcTag("tool", Lrc.Builder::setRe, String::toString),
        LrcTag("ve", Lrc.Builder::setVe, String::toString),
    )

    private fun getTagToValue(line: String) = LRC_TAG_VALUE_REGEX.matchEntire(line)?.let {
        val (tag, value) = it.destructured
        tag to value
    }

    private fun parseLrcLine(tag: String, value: String) = LRC_TIME_REGEX.matchEntire(tag)?.let {
        val (minutes, seconds, hundredths) = it.destructured

        val timeMs = (minutes.toLong() * 60 * 1000)
            .plus(seconds.toLong() * 1000)
            .plus(hundredths.toLong() * 10)

        Lrc.Line(timeMs, value)
    }

    fun parseLrcLine(lrcLine: String) = getTagToValue(lrcLine)?.let {
        val (tag, value) = it
        parseLrcLine(tag, value)
    }

    /**
     * Parse the content of an LRC file.
     */
    fun parseLrc(lrc: String) = Lrc.Builder()
        .apply {
            for (line in lrc.lineSequence()) {
                val (tag, value) = getTagToValue(line) ?: continue

                parseLrcLine(tag, value)?.also {
                    addLine(it)
                } ?: allLrcTags.firstOrNull { lrcTag ->
                    lrcTag.tryParseAndSet(this, tag)
                } ?: run {
                    Log.e(LOG_TAG, "Unknown LRC tag: $tag")
                }
            }
        }
        .build()
}
