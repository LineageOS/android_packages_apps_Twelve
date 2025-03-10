/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:UseSerializers(
    InstantSerializer::class,
    UriSerializer::class,
)

package org.lineageos.twelve.datasources.soundcloud.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.lineageos.twelve.datasources.soundcloud.serializers.InstantSerializer
import org.lineageos.twelve.datasources.soundcloud.serializers.UriSerializer
import java.time.Instant

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Selection(
    val urn: String,
    @SerialName("query_urn") val queryUrn: String,
    val title: String,
    val description: String?,
    @SerialName("tracking_feature_name") val trackingFeatureName: String,
    @SerialName("last_updated") val lastUpdated: Instant?,
    val style: String?,
    @SerialName("social_proof") val socialProof: String?,
    @SerialName("social_proof_users") val socialProofUsers: List<User>?,
    val items: PaginatedCollection<KindItem>,
    override val kind: Kind,
    val id: String,
) : KindItem
