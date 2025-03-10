/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud.serializers

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.lineageos.twelve.datasources.soundcloud.models.KindItem
import org.lineageos.twelve.datasources.soundcloud.models.Kind
import org.lineageos.twelve.datasources.soundcloud.models.Selection
import org.lineageos.twelve.datasources.soundcloud.models.Playlist
import org.lineageos.twelve.datasources.soundcloud.models.SystemPlaylist
import org.lineageos.twelve.datasources.soundcloud.models.Track
import org.lineageos.twelve.datasources.soundcloud.models.User

class KindItemSerializer : JsonContentPolymorphicSerializer<KindItem>(KindItem::class) {
    override fun selectDeserializer(element: JsonElement) = when (element) {
        is JsonObject -> element["kind"]?.jsonPrimitive?.content?.let {
            when (Kind.fromValue(it)) {
                Kind.PLAYLIST -> Playlist.serializer()
                Kind.SELECTION -> Selection.serializer()
                Kind.SYSTEM_PLAYLIST -> SystemPlaylist.serializer()
                Kind.TRACK -> Track.serializer()
                Kind.USER -> User.serializer()
                null -> throw SerializationException("Unknown element type: $it")
            }
        } ?: throw SerializationException("No kind property")
        else -> throw SerializationException("Unknown JSON type")
    }
}
