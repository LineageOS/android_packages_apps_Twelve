/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * [Playlist] with [Item]s.
 *
 * @param playlist The [Playlist] entity
 * @param items The list of songs
 */
data class PlaylistWithItems(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "playlist_id",
        entityColumn = "item_id",
        associateBy = Junction(PlaylistItemCrossRef::class)
    ) val items: List<Item>,
)
