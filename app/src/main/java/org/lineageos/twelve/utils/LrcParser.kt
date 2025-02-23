/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import android.util.Log
import org.lineageos.twelve.models.Lyrics
import kotlin.reflect.KMutableProperty1

/**
 * https://en.wikipedia.org/wiki/LRC_(file_format)
 */
object LrcParser {
    private val LOG_TAG = LrcParser::class.simpleName!!

    private const val LRC_PREFIX = "["
    private const val LRC_SUFFIX = "]"

    /**
     * LRC lyrics.
     */
    class Lrc {
        data class LrcLine(
            val timeMs: Long?,
            val text: String,
        )

        /**
         * Title of the song.
         */
        var ti: String? = null

        /**
         * Artist performing this song.
         */
        var ar: String? = null

        /**
         * Album the song is from.
         */
        var al: String? = null

        /**
         * Author of the song.
         */
        var au: String? = null

        /**
         * Lyricist of the song.
         */
        var lr: String? = null

        /**
         * Length of the song in milliseconds.
         */
        var lengthMs: Long? = null

        /**
         * Author of the LRC file (not the song).
         */
        var by: String? = null

        /**
         * Specifies a global offset value for the lyric times, in milliseconds. The
         * value is prefixed with either + or -, with + causing lyrics to appear sooner.
         */
        var offsetMs: Long? = null

        /**
         * The player or editor that created the LRC file.
         */
        var re: String? = null

        /**
         * The version of the program.
         */
        var ve: String? = null

        /**
         * The lyrics lines.
         */
        var lines = mutableListOf<LrcLine>()

        fun build() = Lyrics.Builder()
            .apply {
                for (line in lines) {
                    addLine(line.text, line.timeMs)
                }
            }
            .build()
    }

    open class LrcTag<T>(
        private val name: String,
        private val property: KMutableProperty1<Lrc, T?>,
        private val parser: String.() -> T?
    ) {
        fun tryParseAndSet(lrc: Lrc, tag: String): Boolean {
            val tagPrefix = "$name:"
            if (!tag.startsWith(tagPrefix)) {
                return false
            }

            val value = tag.removePrefix(tagPrefix).trim()
            property.set(lrc, parser(value))

            return true
        }
    }

    private fun <T> makeLrcTag(
        name: String,
        property: KMutableProperty1<Lrc, T?>,
        parser: String.() -> T?
    ) = object : LrcTag<T>(name, property, parser) {}

    private val allLrcTags = mutableListOf(
        makeLrcTag("ti", Lrc::ti, String::toString),
        makeLrcTag("ar", Lrc::ar, String::toString),
        makeLrcTag("al", Lrc::al, String::toString),
        makeLrcTag("au", Lrc::au, String::toString),
        makeLrcTag("lr", Lrc::lr, String::toString),
        makeLrcTag("length", Lrc::lengthMs) {
            val (minutes, seconds) = split(":", limit = 2)
            (minutes.toLong() * 60 * 1000) + (seconds.toLong() * 1000)
        },
        makeLrcTag("by", Lrc::by, String::toString),
        makeLrcTag("offset", Lrc::offsetMs) {
            when {
                startsWith("+") -> substring(1).toLong()
                startsWith("-") -> substring(1).toLong() * -1
                else -> null
            }
        },
        makeLrcTag("re", Lrc::re, String::toString),
        makeLrcTag("tool", Lrc::re, String::toString),
        makeLrcTag("ve", Lrc::ve, String::toString),
    )

    fun parseLrcLine(lrcLine: String) = lrcLine.takeIf { it.startsWith("[") }

    fun parseLrc(lrc: String) = Lrc()
        .apply {
            for (line in lrc.lineSequence()) {
                if (!line.startsWith(LRC_PREFIX)) {
                    continue
                }

                val (tag, value) = line.removePrefix(LRC_PREFIX).split(LRC_SUFFIX, limit = 2)

                allLrcTags.find { lrcTag ->
                    lrcTag.tryParseAndSet(this, tag)
                } ?: run {
                    Log.e(LOG_TAG, "Unknown LRC tag: $tag")
                }
            }
        }
        .build()
}
