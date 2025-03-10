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

@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Playlist(
    val title: String,
    val id: Int,
    val kind: String,
    val artworkUrl: Uri,
    val createdAt: String, // TODO: Timestamp
    val description: String,
    val downloadable: Boolean,
    val duration: Int, // TODO: ms?
    val ean: String,
    val embeddableBy: String,
    val genre: String,
    val labelId: Int,
    val labelName: String,
    val lastModified: String, // TODO: Timestamp
    val license: String,
    val permalink: String,
    val permalinkUrl: Uri,
    val playlistType: String,
    val purchaseTitle: String,
    val purchaseUrl: Uri,
    val release: String,
    val releaseDay: Int,
    val releaseMonth: Int,
    val releaseYear: Int,
    val sharing: String,
    val streamable: Boolean,
    val tagList: String,
    val trackCount: Int,
    val tracks: List<Track>,
    val type: String,
    val uri: Uri,
    val user: User? = null,
    val userId: Int,
    val likesCount: Int,
    val label: User? = null,
    val tracksUri: Uri? = null,
    val tags: String? = null,
)
