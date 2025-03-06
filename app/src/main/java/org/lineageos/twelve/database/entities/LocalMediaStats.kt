/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.twelve.database.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Database entity for local media stats
 *
 * @param itemId The [Item] unique ID
 * @param playCount The number of times the media has been played
 */
@Entity(
    indices = [
        Index(value = ["item_id"], unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = Item::class,
            parentColumns = ["item_id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE,
        ),
    ]
)
data class LocalMediaStats(
    @PrimaryKey @ColumnInfo(name = "item_id") val itemId: Long,
    @ColumnInfo(name = "play_count", defaultValue = "1") val playCount: Long,
)
