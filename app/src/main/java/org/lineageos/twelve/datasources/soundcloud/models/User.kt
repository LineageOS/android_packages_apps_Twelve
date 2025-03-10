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
 * User.
 *
 * @param avatarUrl URL to a JPEG image
 * @param city City
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
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class User(
    @SerialName("avatar_url") val avatarUrl: Uri,
    @SerialName("blocked_tracks_count") val blockedTrackCount: Int? = null,
    val city: String?,
    @SerialName("comments_count") val commentsCount: Int? = null,
    @SerialName("consumer_subscriptions") val consumerSubscriptions: List<Subscription>? = null,
    @SerialName("consumer_subscription") val consumerSubscription: Subscription? = null,
    @SerialName("country_code") val countryCode: String?,
    val cpp: String? = null,
    @SerialName("created_at") val createdAt: Instant? = null,
    @SerialName("creator_subscriptions") val creatorSubscriptions: List<Subscription>? = null,
    @SerialName("creator_subscription") val creatorSubscription: Subscription? = null,
    @SerialName("date_of_birth") val dateOfBirth: DateOfBirth? = null,
    @SerialName("default_license") val defaultLicense: String? = null,
    @SerialName("default_tracks_feedable") val defaultTracksFeedable: Boolean? = null,
    val description: String? = null,
    @SerialName("downloads_disabled") val downloadsDisabled: Boolean? = null,
    @SerialName("downloads_disabled_reason") val downloadsDisabledReason: String? = null,
    @SerialName("first_name") val firstName: String,
    @SerialName("followers_count") val followersCount: Int,
    @SerialName("followings_count") val followingsCount: Int? = null,
    @SerialName("full_name") val fullName: String,
    val gender: String? = null,
    @SerialName("groups_count") val groupsCount: Int? = null,
    @SerialName("hidden_tracks_count") val hiddenTracksCount: Int? = null,
    val id: Int,
    override val kind: Kind,
    @SerialName("last_modified") val lastModified: Instant,
    @SerialName("last_name") val lastName: String,
    @SerialName("likes_count") val likesCount: Int? = null,
    @SerialName("playlist_likes_count") val playlistLikesCount: Int? = null,
    val locale: String? = null,
    val permalink: String,
    @SerialName("permalink_url") val permalinkUrl: Uri,
    @SerialName("playlist_count") val playlistCount: Int? = null,
    @SerialName("primary_email") val primaryEmail: String? = null,
    @SerialName("primary_email_confirmed") val primaryEmailConfirmed: Boolean? = null,
    @SerialName("primary_email_sha256") val primaryEmailSha256: String? = null,
    @SerialName("private_playlists_count") val privatePlaylistsCount: Int? = null,
    @SerialName("private_tracks_count") val privateTracksCount: Int? = null,
    val quota: Quota? = null,
    @SerialName("reposts_count") val repostsCount: Int? = null,
    @SerialName("track_count") val trackCount: Int? = null,
    val uri: Uri,
    val urn: String,
    val username: String,
    val verified: Boolean,
    val visuals: Visuals? = null,
    val confirmed: Boolean? = null,
    val badges: Badges,
    @SerialName("analytics_id") val analyticsId: String? = null,
    @SerialName("consent_management_jwt") val consentManagementJwt: ConsentManagementJwt? = null,
    @SerialName("station_urn") val stationUrn: String? = null,
    @SerialName("station_permalink") val stationPermalink: String? = null,
    @SerialName("marketing_ids") val marketingIds: MarketingIds? = null,
    val ppid: String? = null,
    @SerialName("spotlight_limit") val spotlightLimit: Int? = null,
) : Item {
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

    @Suppress("PROVIDED_RUNTIME_TOO_LOW")
    @Serializable
    data class ConsentManagementJwt(
        val userId: String,
        val jwt: String,
    )

    @Suppress("PROVIDED_RUNTIME_TOO_LOW")
    @Serializable
    data class DateOfBirth(
        val month: Int,
        val year: Int,
    )

    @Suppress("PROVIDED_RUNTIME_TOO_LOW")
    @Serializable
    data class MarketingIds(
        val gtm: String,
    )

    @Suppress("PROVIDED_RUNTIME_TOO_LOW")
    @Serializable
    data class Quota(
        @SerialName("unlimited_upload_quota") val unlimitedUploadQuota: Boolean,
        @SerialName("upload_seconds_limit") val uploadSecondsLimit: Int,
        @SerialName("upload_seconds_used") val uploadSecondsUsed: Int,
        @SerialName("upload_seconds_left") val uploadSecondsLeft: Int,
        @SerialName("upload_tracks_used") val uploadTracksUsed: Int,
        @SerialName("unlimited_upload_duration_quota") val unlimitedUploadDurationQuota: Boolean,
        @SerialName("unlimited_upload_track_quota") val unlimitedUploadTrackQuota: Boolean,
    )

    @Suppress("PROVIDED_RUNTIME_TOO_LOW")
    @Serializable
    data class Subscription(
        val product: Product,
    ) {
        @Suppress("PROVIDED_RUNTIME_TOO_LOW")
        @Serializable
        data class Product(
            val id: String,
        )
    }

}
