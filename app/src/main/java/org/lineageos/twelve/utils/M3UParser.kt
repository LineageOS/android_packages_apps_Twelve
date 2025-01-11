/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import org.lineageos.twelve.models.M3UPlaylist
import java.io.InputStream

object M3UParser {
    fun parse(inputStream: InputStream) = inputStream.bufferedReader().use { reader ->
        M3UPlaylist(
            entries = reader.readLines()
                .map { it.trim() }
                .filter {
                    it.isNotBlank() && !it.startsWith("#") && !it.startsWith("http")
                }
        )
    }
}
