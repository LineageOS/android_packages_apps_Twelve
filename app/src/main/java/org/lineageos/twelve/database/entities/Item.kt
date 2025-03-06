/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A general purpose item representing an audio file.
 *
 * @param id The unique item ID
 * @param audioUri The [Uri] of this item
 * @param count The play count of this item
 */
@Entity(
    indices = [
        Index(value = ["audio_uri"], unique = true),
    ],
)
data class Item(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "item_id") val id: Long,
    @ColumnInfo(name = "audio_uri") val audioUri: Uri,
    @ColumnInfo(name = "count", defaultValue = "0") val count: Long = 0,
)
