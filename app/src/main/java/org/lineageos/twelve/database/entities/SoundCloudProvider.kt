/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * SoundCloud provider entity.
 *
 * @param id The unique ID of this instance
 * @param name The name of this provider
 * @param clientId The client ID
 */
@Entity
data class SoundCloudProvider(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "soundcloud_provider_id") val id: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "client_id") val clientId: String,
    @ColumnInfo(name = "oauth_token") val oAuthToken: String?,
)
