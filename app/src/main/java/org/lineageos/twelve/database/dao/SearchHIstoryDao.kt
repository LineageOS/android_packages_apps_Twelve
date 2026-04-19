/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.twelve.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.lineageos.twelve.database.entities.SearchHistory

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM SearchHistory ORDER BY searched_at DESC LIMIT 10")
    fun getAll(): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SearchHistory)

    @Delete
    suspend fun delete(item: SearchHistory)
}
