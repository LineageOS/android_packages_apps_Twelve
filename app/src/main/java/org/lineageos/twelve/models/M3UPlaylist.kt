/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import java.io.File

data class M3UPlaylist(
    val displayTitle: String? = null,
    val entries: List<String>
) {
    /**
     * Get the base names of the entries, should also work for relative paths and Windows paths.
     */
    fun getBaseNames() = entries.map { File(it).name }

    class Builder {
        private var displayTitle: String? = null
        private val entries = mutableListOf<String>()

        fun setDisplayTitle(displayTitle: String?) = also {
            this.displayTitle = this.displayTitle ?: displayTitle
        }

        fun addEntry(entry: String) = also {
            entries.add(entry)
        }

        fun build() = M3UPlaylist(
            displayTitle = displayTitle,
            entries = entries.toList(),
        )
    }
}
