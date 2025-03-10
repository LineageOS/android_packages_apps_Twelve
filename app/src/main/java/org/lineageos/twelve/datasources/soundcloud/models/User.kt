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
 * @param firstName First name
 * @param followersCount Number of followers
 * @param fullName First and last name
 * @param id Unique identifier
 * @param kind Kind of resource
 * @param lastModified Last modified datetime
 * @param lastName Last name
 * @param permalink Permalink of the resource
 * @param permalinkUrl URL to the SoundCloud.com page
 * @param permalinkUrl URL to the SoundCloud.com page
 * @param uri URL to the SoundCloud API for this user
 * @param urn Unique resource name
 * @param username Username
 * @param city City
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class User(
    @SerialName("avatar_url") val avatarUrl: Uri,
    @SerialName("first_name") val firstName: String,
    @SerialName("followers_count") val followersCount: Int,
    @SerialName("full_name") val fullName: String,
    val id: Int,
    val kind: String,
    @SerialName("last_modified") val lastModified: String, // TODO: "date-time"
    @SerialName("last_name") val lastName: String,
    val permalink: String,
    @SerialName("permalink_url") val permalinkUrl: Uri,
    val uri: Uri,
    val urn: String,
    val username: String,
    val verified: Boolean,
    val city: String,
    @SerialName("country_code") val countryCode: String,
    val badges: Badges,
    @SerialName("station_urn") val stationUrn: String,
    @SerialName("station_permalink") val stationPermalink: String,
) {
    /**
     * [User]'s badges.
     *
     * @param pro Whether the user is pro
     * @param creatorMidTier Whether the user is a mid-tier creator
     * @param proUnlimited Whether the user has unlimited pro
     * @param verified Whether the user is verified
     */
    @Suppress("PROVIDED_RUNTIME_TOO_LOW")
    @Serializable
    data class Badges(
        val pro: Boolean,
        @SerialName("creator_mid_tier") val creatorMidTier: Boolean,
        @SerialName("pro_unlimited") val proUnlimited: Boolean,
        val verified: Boolean,
    )
}
