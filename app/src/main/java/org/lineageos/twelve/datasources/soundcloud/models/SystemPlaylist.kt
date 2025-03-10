/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:UseSerializers(
    InstantSerializer::class,
    UriSerializer::class,
)

package org.lineageos.twelve.datasources.soundcloud.models

import android.net.Uri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.lineageos.twelve.datasources.soundcloud.serializers.InstantSerializer
import org.lineageos.twelve.datasources.soundcloud.serializers.UriSerializer
import java.time.Instant

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class SystemPlaylist(
    val urn: String,
    @SerialName("query_urn") val queryUrn: String,
    val permalink: String,
    @SerialName("permalink_url") val permalinkUrl: Uri,
    val title: String,
    val description: String,
    @SerialName("short_title") val shortTitle: String,
    @SerialName("short_description") val shortDescription: String,
    @SerialName("tracking_feature_name") val trackingFeatureName: String,
    @SerialName("playlist_type") val playlistType: String,
    @SerialName("last_updated") val lastUpdated: Instant?,
    @SerialName("artwork_url") val artworkUrl: Uri,
    @SerialName("calculated_artwork_url") val calculatedArtworkUrl: Uri,
    @SerialName("likes_count") val likesCount: Int,
    val seed: Seed?,
    val tracks: List<PartialTrack>,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("made_for") val madeFor: User?,
    val user: User,
    override val kind: Kind,
    val id: String,
) : Item {
    @Suppress("PROVIDED_RUNTIME_TOO_LOW")
    @Serializable
    data class Seed(
        val urn: String,
        val permalink: Uri,
    )
}
