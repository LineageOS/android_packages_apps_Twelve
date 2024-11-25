/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.lineageos.twelve.database.entities.JellyfinProvider
import org.lineageos.twelve.database.entities.SubsonicProvider

@Dao
interface JellyfinProviderDao {
    /**
     * Add a new jellyfin provider to the database.
     */
    @Query(
        """
            INSERT INTO JellyfinProvider (name, url, api_key)
            VALUES (:name, :url, :apiKey)
        """
    )
    suspend fun create(
        name: String,
        url: String,
        apiKey: String,
    ): Long

    /**
     * Update a jellyfin provider.
     */
    @Query(
        """
            UPDATE JellyfinProvider
            SET name = :name,
                url = :url,
                api_key = :apiKey
            WHERE jellyfin_provider_id = :jellyfinProviderId
        """
    )
    suspend fun update(
        jellyfinProviderId: Long,
        name: String,
        url: String,
        apiKey: String,
    )

    /**
     * Delete a jellyfin provider from the database.
     */
    @Query("DELETE FROM JellyfinProvider WHERE jellyfin_provider_id = :jellyfinProviderId")
    suspend fun delete(jellyfinProviderId: Long)

    /**
     * Fetch all jellyfin providers from the database.
     */
    @Query("SELECT * FROM JellyfinProvider")
    fun getAll(): Flow<List<JellyfinProvider>>

    /**
     * Fetch a jellyfin provider by its ID from the database.
     */
    @Query("SELECT * FROM JellyfinProvider WHERE jellyfin_provider_id = :jellyfinProviderId")
    fun getById(jellyfinProviderId: Long): Flow<JellyfinProvider?>
}
