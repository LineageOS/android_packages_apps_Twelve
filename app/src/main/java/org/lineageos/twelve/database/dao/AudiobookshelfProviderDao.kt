/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.lineageos.twelve.database.entities.AudiobookshelfProvider

@Dao
interface AudiobookshelfProviderDao {
    /**
     * Add a new audiobookshelf provider to the database.
     */
    @Query(
        """
            INSERT INTO AudiobookshelfProvider (name, url, username, password)
            VALUES (:name, :url, :username, :password)
        """
    )
    suspend fun create(
        name: String,
        url: String,
        username: String,
        password: String,
    ): Long

    /**
     * Update a audiobookshelf provider.
     */
    @Query(
        """
            UPDATE AudiobookshelfProvider
            SET name = :name,
                url = :url,
                username = :username,
                password = :password,
                token = NULL
            WHERE audiobookshelf_provider_id = :jellyfinProviderId
        """
    )
    suspend fun update(
        jellyfinProviderId: Long,
        name: String,
        url: String,
        username: String,
        password: String,
    )

    /**
     * Delete a audiobookshelf provider from the database.
     */
    @Query("DELETE FROM AudiobookshelfProvider WHERE audiobookshelf_provider_id = :audiobookshelfProviderId")
    suspend fun delete(audiobookshelfProviderId: Long)

    /**
     * Fetch all audiobookshelf providers from the database.
     */
    @Query("SELECT * FROM AudiobookshelfProvider")
    fun getAll(): Flow<List<AudiobookshelfProvider>>

    /**
     * Fetch a audiobookshelf provider by its ID from the database.
     */
    @Query("SELECT * FROM AudiobookshelfProvider WHERE audiobookshelf_provider_id = :audiobookshelfProviderId")
    fun getById(audiobookshelfProviderId: Long): Flow<AudiobookshelfProvider?>

    /**
     * Fetch the token of a audiobookshelf provider by its ID from the database.
     */
    @Query("SELECT token FROM AudiobookshelfProvider WHERE audiobookshelf_provider_id = :audiobookshelfProviderId")
    fun getToken(audiobookshelfProviderId: Long): String?

    /**
     * Update the token of a audiobookshelf provider by its ID in the database.
     */
    @Query(
        """
            UPDATE AudiobookshelfProvider
            SET token = :token
            WHERE audiobookshelf_provider_id = :audiobookshelfProviderId
        """
    )
    fun updateToken(audiobookshelfProviderId: Long, token: String)
}
