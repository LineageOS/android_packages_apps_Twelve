/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:UseSerializers(UUIDSerializer::class)

package org.lineageos.twelve.datasources.jellyfin.models

import java.util.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.lineageos.twelve.datasources.jellyfin.serializers.UUIDSerializer

@Serializable data class PlaylistItems(@SerialName("ItemIds") val itemIds: List<UUID>)
