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

/**
 * Track.
 *
 * @param artworkUrl The artwork URL
 * @param caption Track caption
 * @param commentable Whether this track can be commented
 * @param commentCount Number of comments
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Track(
    @SerialName("artwork_url") val artworkUrl: Uri?,
    val caption: String?,
    val commentable: Boolean,
    @SerialName("comment_count") val commentCount: Int? = null,
    @SerialName("created_at") val createdAt: Instant,
    val description: String?,
    val downloadable: Boolean,
    @SerialName("download_count") val downloadCount: Int?,
    val duration: Long,
    @SerialName("full_duration") val fullDuration: Long,
    @SerialName("embeddable_by") val embeddableBy: String,
    val genre: String?,
    @SerialName("has_download_left") val hasDownloadLeft: Boolean = true,
    val id: Int,
    override val kind: Kind,
    @SerialName("label_name") val labelName: String?,
    @SerialName("last_modified") val lastModified: Instant,
    val license: String,
    @SerialName("likes_count") val likesCount: Int?,
    val permalink: String,
    @SerialName("permalink_url") val permalinkUrl: Uri,
    @SerialName("playback_count") val playbackCount: Int?,
    val public: Boolean,
    @SerialName("publisher_metadata") val publisherMetadata: PublisherMetadata?,
    @SerialName("purchase_title") val purchaseTitle: String?,
    @SerialName("purchase_url") val purchaseUrl: Uri?,
    @SerialName("release_date") val releaseDate: Instant?,
    @SerialName("reposts_count") val repostsCount: Int,
    @SerialName("secret_token") val secretToken: String?,
    val sharing: String,
    val state: String,
    val streamable: Boolean,
    @SerialName("tag_list") val tagList: String,
    val title: String,
    val uri: Uri,
    val urn: String,
    @SerialName("user_id") val userId: Int?,
    val visuals: Visuals?,
    @SerialName("waveform_url") val waveformUrl: Uri,
    @SerialName("display_date") val displayDate: Instant,
    val media: Media,
    @SerialName("station_urn") val stationUrn: String,
    @SerialName("station_permalink") val stationPermalink: String,
    @SerialName("track_authorization") val trackAuthorization: String,
    @SerialName("monetization_model") val monetizationModel: String,
    val policy: String,
    val user: User,
) : Item
