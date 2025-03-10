/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:UseSerializers(UriSerializer::class)

package org.lineageos.twelve.datasources.soundcloud.models

import android.net.Uri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.lineageos.twelve.datasources.soundcloud.serializers.UriSerializer

/**
 * Sets.
 *
 * @param collection List of [Set]
 * @param nextHref URL to get the next results
 * @param queryUrn Query URN
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Sets(
    val collection: List<Set>,
    @SerialName("next_href") val nextHref: Uri?,
    @SerialName("query_urn") val queryUrn: String?,
)
