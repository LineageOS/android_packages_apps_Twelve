/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.lineageos.twelve.database.entities.Item

@Dao
@Suppress("FunctionName")
interface ItemDao {
    /**
     * Insert a new item.
     */
    @Query("INSERT INTO Item (audio_uri) VALUES (:audioUri)")
    suspend fun insert(audioUri: Uri): Long

    /**
     * Delete an item from the database.
     */
    @Query("DELETE FROM Item WHERE audio_uri = :audioUri")
    suspend fun delete(audioUri: Uri)

    /**
     * Get the item by its [Uri].
     */
    @Query("SELECT * FROM Item WHERE audio_uri = :audioUri")
    suspend fun getByUri(audioUri: Uri): Item?

    /**
     * Get or insert the item by its [Uri].
     */
    @Transaction
    suspend fun getOrInsert(audioUri: Uri) = getByUri(audioUri) ?: run {
        Item(insert(audioUri), audioUri)
    }

    /**
     * Get the item's ID given its [Uri].
     */
    @Query("SELECT item_id FROM Item WHERE audio_uri = :audioUri")
    fun _getIdByUri(audioUri: Uri): Long?

    /**
     * Get the item's ID's flow given its [Uri].
     */
    @Query("SELECT item_id FROM Item WHERE audio_uri = :audioUri")
    fun _getIdFlowByUri(audioUri: Uri): Flow<Long?>
}
