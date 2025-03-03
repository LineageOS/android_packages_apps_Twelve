/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Playlist(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "playlist_id") val id: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "last_modified") val lastModified: Long,
    @ColumnInfo(name = "track_count", defaultValue = "0") val trackCount: Long,
    @ColumnInfo(name = "favorite", defaultValue = "FALSE") val favorite: Boolean = false
)
