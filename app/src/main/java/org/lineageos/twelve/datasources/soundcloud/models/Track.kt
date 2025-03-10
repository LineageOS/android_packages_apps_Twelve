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
 * Track.
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Track(
    val title: String,
    @SerialName("artwork_url") val artworkUrl: Uri,
    val bpm: Int,
    @SerialName("comment_count") val commentCount: Int,
    val commentable: Boolean,
    @SerialName("created_at") val createdAt: String, // TODO: Timestamp
    val description: String,
    @SerialName("download_count") val downloadCount: Int,
    val downloadable: String,
    val duration: Int, // TODO: ms?
    @SerialName("favoritings_count") val favoritingsCount: Int,
    val genre: String,
    val id: Int,
    val isrc: String,
    @SerialName("key_signature") val keySignature: String,
    val kind: String,
    @SerialName("label_name") val labelName: String,
    val license: String,
    @SerialName("permalink_url") val permalinkUrl: Uri,
    @SerialName("playback_count") val playbackCount: Int,
    @SerialName("purchase_title") val purchaseTitle: String,
    @SerialName("purchase_url") val purchaseUrl: Uri,
    val release: String,
    @SerialName("release_day") val releaseDay: Int,
    @SerialName("release_month") val releaseMonth: Int,
    @SerialName("release_year") val releaseYear: Int,
    val sharing: String,
    @SerialName("stream_url") val streamUrl: Uri,
    val streamable: Boolean,
    @SerialName("tag_list") val tagList: String,
    val uri: Uri,
    val user: User? = null,
    @SerialName("user_favorite") val userFavorite: Boolean,
    @SerialName("user_playback_count") val userPlaybackCount: Int,
    @SerialName("waveform_url") val waveformUrl: Uri,
    @SerialName("available_country_codes") val availableCountryCodes: String,
    val access: Access,
    @SerialName("download_url") val downloadUrl: Uri,
    @SerialName("reposts_count") val repostsCount: Int,
    @SerialName("secret_uri") val secretUri: Uri,
)
