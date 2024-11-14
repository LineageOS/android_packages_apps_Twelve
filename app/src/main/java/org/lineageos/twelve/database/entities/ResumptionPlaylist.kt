/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ResumptionPlaylist(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "resumption_id") val id: Long,
    @ColumnInfo(name = "start_index") val startIndex: Int,
    @ColumnInfo(name = "start_position_ms") val startPositionMs: Long,
)