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
import org.lineageos.twelve.datasources.soundcloud.serializers.TrackListSerializer
import org.lineageos.twelve.datasources.soundcloud.serializers.UriSerializer
import java.time.Instant

/**
 * A set.
 *
 * @param artworkUrl URL of the artwork
 * @param createdAt Date of creation
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Set(
    @SerialName("artwork_url") val artworkUrl: Uri,
    @SerialName("created_at") val createdAt: Instant,
    val description: String?,
    val duration: Long,
    @SerialName("embeddable_by") val embeddableBy: String,
    val genre: String,
    val id: Int,
    val kind: String,
    @SerialName("label_name") val labelName: String?,
    @SerialName("last_modified") val lastModified: Instant,
    val license: String,
    @SerialName("likes_count") val likesCount: Int,
    @SerialName("managed_by_feeds") val managedByFeeds: Boolean,
    val permalink: String,
    @SerialName("permalink_url") val permalinkUrl: Uri,
    val public: Boolean,
    @SerialName("purchase_title") val purchaseTitle: String?,
    @SerialName("purchase_url") val purchaseUrl: Uri?,
    @SerialName("release_date") val releaseDate: Instant,
    @SerialName("reposts_count") val repostsCount: Int,
    @SerialName("secret_token") val secretToken: String?,
    val sharing: String,
    @SerialName("tag_list") val tagList: String,
    val title: String,
    val uri: Uri,
    @SerialName("user_id") val userId: Int,
    @SerialName("set_type") val setType: String,
    @SerialName("is_album") val isAlbum: Boolean,
    @SerialName("published_at") val publishedAt: Instant,
    @SerialName("display_date") val displayDate: Instant,
    val user: User,
    @Serializable(with = TrackListSerializer::class) val tracks: List<Track>,
    @SerialName("track_count") val trackCount: Int,
)
