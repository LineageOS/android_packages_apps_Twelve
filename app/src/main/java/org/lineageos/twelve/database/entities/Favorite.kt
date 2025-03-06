/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A table representing the favorite user items.
 *
 * @param itemId The [Item] unique ID
 * @param addedAt The date and time of when this item was added to the favorites
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
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ]
)
data class Favorite(
    @PrimaryKey @ColumnInfo(name = "item_id") val itemId: Long,
    @ColumnInfo(name = "added_at") val addedAt: Instant,
)
