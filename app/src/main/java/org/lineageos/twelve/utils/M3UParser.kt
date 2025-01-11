/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import android.util.Log
import org.lineageos.twelve.models.M3UPlaylist
import java.io.InputStream

object M3UParser {
    private val LOG_TAG = M3UParser::class.simpleName!!

    private const val M3U_TAG_PREFIX = "#"

    private const val M3U_HEADER = "#EXTM3U"
    private const val M3U_PLAYLIST_TAG = "#PLAYLIST:"

    fun parse(inputStream: InputStream): M3UPlaylist? = inputStream.bufferedReader().use { reader ->
        if (reader.readLine() != M3U_HEADER) {
            Log.e(LOG_TAG, "This is not a M3U file")
            return null
        }

        val builder = M3UPlaylist.Builder()

        for (line in reader.lineSequence()) {
            val trimmedLine = line.trim()

            when {
                trimmedLine.isBlank() -> {
                    // Skip empty lines
                }

                trimmedLine.startsWith(M3U_PLAYLIST_TAG) -> {
                    builder.setDisplayTitle(trimmedLine.removePrefix(M3U_PLAYLIST_TAG))
                }

                trimmedLine.startsWith(M3U_TAG_PREFIX) -> {
                    // Skip unknown tag
                    Log.i(LOG_TAG, "Skipping unknown tag: $trimmedLine")
                }

                trimmedLine.lowercase().startsWith("http") -> {
                    // Skip non-local URIs
                    Log.i(LOG_TAG, "Skipping HTTP URL: $trimmedLine")
                }

                else -> builder.addEntry(trimmedLine)
            }
        }

        builder.build()
    }
}
