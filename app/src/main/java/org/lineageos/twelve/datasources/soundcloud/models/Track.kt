/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:UseSerializers(UriSerializer::class)

package org.lineageos.twelve.datasources.soundcloud.models

import android.net.Uri
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
    val artworkUrl: Uri,
    val bpm: Int,
    val commentCount: Int,
    val commentable: Boolean,
    val createdAt: String, // TODO: Timestamp
    val description: String,
    val downloadCount: Int,
    val downloadable: String,
    val duration: Int, // TODO: ms?
    val favoritingsCount: Int,
    val genre: String,
    val id: Int,
    val isrc: String,
    val keySignature: String,
    val kind: String,
    val labelName: String,
    val license: String,
    val permalinkUrl: Uri,
    val playbackCount: Int,
    val purchaseTitle: String,
    val purchaseUrl: Uri,
    val release: String,
    val releaseDay: Int,
    val releaseMonth: Int,
    val releaseYear: Int,
    val sharing: String,
    val streamUrl: Uri,
    val streamable: Boolean,
    val tagList: String,
    val uri: Uri,
    val user: User? = null,
    val userFavorite: Boolean,
    val userPlaybackCount: Int,
    val waveformUrl: Uri,
    val availableCountryCodes: String,
    val access: String, // TODO: Enum
    val downloadUrl: Uri,
    val repostsCount: Int,
    val secretUri: Uri,
)
