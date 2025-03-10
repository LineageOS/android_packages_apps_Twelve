/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for lists that contains partial objects. Those objects will be ignored.
 *
 * @param typeSerializer The serializer for the type
 * @param requiredFields The fields that are required even for a partial entry
 */
@OptIn(ExperimentalSerializationApi::class)
abstract class LaxedListSerializer<T : Any>(
    typeSerializer: KSerializer<T>,
    requiredFields: Set<String>,
) : KSerializer<List<T>> {
    private val laxedSerializer = object : KSerializer<T?> {
        override val descriptor = typeSerializer.descriptor.nullable

        override fun deserialize(decoder: Decoder) = try {
            typeSerializer.deserialize(decoder)
        } catch (e: MissingFieldException) {
            if (e.missingFields.intersect(requiredFields).isEmpty()) {
                null
            } else {
                throw e
            }
        }

        override fun serialize(encoder: Encoder, value: T?) = value?.let {
            typeSerializer.serialize(encoder, it)
        } ?: encoder.encodeNull()
    }

    private val listSerializer = ListSerializer(laxedSerializer)

    override val descriptor = listSerializer.descriptor

    override fun deserialize(decoder: Decoder) = listSerializer.deserialize(decoder).filterNotNull()

    override fun serialize(
        encoder: Encoder, value: List<T>
    ) = listSerializer.serialize(encoder, value)
}
