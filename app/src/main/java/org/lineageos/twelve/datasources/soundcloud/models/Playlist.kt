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
 * A playlist.
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Playlist(
    val title: String,
    val id: Int,
    val kind: String,
    @SerialName("artwork_url") val artworkUrl: Uri,
    @SerialName("created_at") val createdAt: String, // TODO: Timestamp
    val description: String,
    val downloadable: Boolean,
    val duration: Int, // TODO: ms?
    val ean: String,
    @SerialName("embeddable_by") val embeddableBy: String,
    val genre: String,
    @SerialName("label_id") val labelId: Int,
    @SerialName("label_name") val labelName: String,
    @SerialName("last_modified") val lastModified: String, // TODO: Timestamp
    val license: String,
    val permalink: String,
    @SerialName("permalink_url") val permalinkUrl: Uri,
    @SerialName("playlist_type") val playlistType: String,
    @SerialName("purchase_title") val purchaseTitle: String,
    @SerialName("purchase_url") val purchaseUrl: Uri,
    val release: String,
    @SerialName("release_day") val releaseDay: Int,
    @SerialName("release_month") val releaseMonth: Int,
    @SerialName("release_year") val releaseYear: Int,
    val sharing: String,
    val streamable: Boolean,
    @SerialName("tag_list") val tagList: String,
    @SerialName("track_count") val trackCount: Int,
    val tracks: List<Track>,
    val type: String,
    val uri: Uri,
    val user: User? = null,
    @SerialName("user_id") val userId: Int,
    @SerialName("likes_count") val likesCount: Int,
    val label: User? = null,
    @SerialName("tracks_uri") val tracksUri: Uri? = null,
    val tags: String? = null,
)
