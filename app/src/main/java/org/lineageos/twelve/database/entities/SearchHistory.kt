/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.twelve.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * A table representing the search history.
 *
 * @param query The search query
 * @param searchedAt The date and time of when this query was last searched
 */
@Entity
data class SearchHistory(
    @PrimaryKey @ColumnInfo(name = "query") val query: String,
    @ColumnInfo(name = "searched_at") val searchedAt: Instant,
)
