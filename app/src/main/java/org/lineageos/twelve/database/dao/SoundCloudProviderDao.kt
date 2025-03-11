/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.lineageos.twelve.database.entities.SoundCloudProvider

@Dao
interface SoundCloudProviderDao {
    /**
     * Add a new SoundCloud provider to the database.
     */
    @Query(
        """
            INSERT INTO SoundCloudProvider (name, client_id, oauth_token)
            VALUES (:name, :clientId, :oAuthToken)
        """
    )
    suspend fun create(
        name: String,
        clientId: String,
        oAuthToken: String?,
    ): Long

    /**
     * Update a SoundCloud provider.
     */
    @Query(
        """
            UPDATE SoundCloudProvider
            SET name = :name,
                client_id = :clientId,
                oauth_token = :oAuthToken
            WHERE soundcloud_provider_id = :soundcloudProviderId
        """
    )
    suspend fun update(
        soundcloudProviderId: Long,
        name: String,
        clientId: String,
        oAuthToken: String?,
    )

    /**
     * Delete a SoundCloud provider from the database.
     */
    @Query("DELETE FROM SoundCloudProvider WHERE soundcloud_provider_id = :soundcloudProviderId")
    suspend fun delete(soundcloudProviderId: Long)

    /**
     * Fetch all SoundCloud providers from the database.
     */
    @Query("SELECT * FROM SoundCloudProvider")
    fun getAll(): Flow<List<SoundCloudProvider>>

    /**
     * Fetch a SoundCloud provider by its ID from the database.
     */
    @Query("SELECT * FROM SoundCloudProvider WHERE soundcloud_provider_id = :soundcloudProviderId")
    fun getById(soundcloudProviderId: Long): Flow<SoundCloudProvider?>
}
