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
 * User.
 *
 * @param avatarUrl URL to a JPEG image
 * @param city City
 * @param country Country
 * @param description Description
 * @param discogsName Discogs name
 * @param firstName First name
 * @param followersCount Number of followers
 * @param followingsCount Number of followed users
 * @param fullName First and last name
 * @param id Unique identifier
 * @param kind Kind of resource
 * @param createdAt Profile creation datetime
 * @param lastModified Last modified datetime
 * @param lastName Last name
 * @param permalink Permalink of the resource
 * @param permalinkUrl URL to the SoundCloud.com page
 * @param plan Subscription plan of the user
 * @param playlistCount Number of public playlists
 * @param publicFavoritesCount Number of favorited public tracks
 * @param repostsCount Number of reposts from user
 * @param trackCount Number of public tracks
 * @param uri API resource URL
 * @param username Username
 * @param website A URL to the website
 * @param websiteTitle A custom title for the website
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class User(
    @SerialName("avatar_url") val avatarUrl: Uri,
    val city: String,
    val country: String,
    val description: String,
    @SerialName("discogs_name") val discogsName: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("followers_count") val followersCount: Int,
    @SerialName("followings_count") val followingsCount: Int,
    @SerialName("full_name") val fullName: String,
    val id: Int,
    val kind: String,
    @SerialName("created_at") val createdAt: String, // TODO: "date-time"
    @SerialName("last_modified") val lastModified: String, // TODO: "date-time"
    @SerialName("last_name") val lastName: String,
    val permalink: String,
    @SerialName("permalink_url") val permalinkUrl: Uri,
    val plan: String,
    @SerialName("playlist_count") val playlistCount: Int,
    @SerialName("public_favorites_count") val publicFavoritesCount: Int,
    @SerialName("reposts_count") val repostsCount: Int,
    @SerialName("track_count") val trackCount: Int,
    val uri: Uri,
    val username: String,
    val website: Uri,
    @SerialName("website_title") val websiteTitle: String,
)
