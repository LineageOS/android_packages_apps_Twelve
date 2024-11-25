/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Jellyfin provider entity.
 */
@Entity
data class JellyfinProvider(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "jellyfin_provider_id") val id: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "device_identifier") val deviceIdentifier: String,
    @ColumnInfo(name = "token") val token: String? = null,
)
