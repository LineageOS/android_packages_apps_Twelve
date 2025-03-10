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
    val avatarUrl: Uri,
    val city: String,
    val country: String,
    val description: String,
    val discogsName: String,
    val firstName: String,
    val followersCount: Int,
    val followingsCount: Int,
    val fullName: String,
    val id: Int,
    val kind: String,
    val createdAt: String, // TODO: "date-time"
    val lastModified: String, // TODO: "date-time"
    val lastName: String,
    val permalink: String,
    val permalinkUrl: Uri,
    val plan: String,
    val playlistCount: Int,
    val publicFavoritesCount: Int,
    val repostsCount: Int,
    val trackCount: Int,
    val uri: Uri,
    val username: String,
    val website: Uri,
    val websiteTitle: String,
)
