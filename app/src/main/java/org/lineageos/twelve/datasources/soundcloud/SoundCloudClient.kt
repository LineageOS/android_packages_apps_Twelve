/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud

import android.net.Uri
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.lineageos.twelve.datasources.soundcloud.models.Access
import org.lineageos.twelve.datasources.soundcloud.models.Playlists
import org.lineageos.twelve.datasources.soundcloud.models.Streams
import org.lineageos.twelve.datasources.soundcloud.models.Track
import org.lineageos.twelve.datasources.soundcloud.models.Tracks
import org.lineageos.twelve.datasources.soundcloud.models.User
import org.lineageos.twelve.utils.Api
import org.lineageos.twelve.utils.ApiRequest
import org.lineageos.twelve.utils.mapToError

/**
 * SoundCloud client.
 *
 * [API reference](https://developers.soundcloud.com/docs/api/explorer/open-api)
 *
 * @param cache OkHttp's [Cache]
 */
class SoundCloudClient(
    private val cache: Cache? = null,
) {
    private val okHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .build()

    private val api = Api(okHttpClient, serverUri)

    /**
     * Returns the authenticated user's information.
     */
    suspend fun me() = ApiRequest.get<User>(
        listOf("me")
    ).execute(api).mapToError()

    suspend fun getLikedTracks(
        limit: Int = 50,
        access: Set<Access> = setOf(Access.PLAYABLE, Access.PREVIEW, Access.BLOCKED),
        linkedPartitioning: Boolean = true,
    ) = ApiRequest.get<Tracks>(
        listOf(
            "me",
            "likes",
            "tracks",
        ),
        queryParameters = listOf(
            "limit" to limit,
            "access_token" to access.joinToString(",") { it.value },
            "linked_partitioning" to linkedPartitioning,
        )
    ).execute(api).mapToError()

    suspend fun getLikedPlaylists(
        limit: Int = 50,
        linkedPartitioning: Boolean = true,
    ) = ApiRequest.get<Playlists>(
        listOf(
            "me",
            "likes",
            "playlists",
        ),
        queryParameters = listOf(
            "limit" to limit,
            "linked_partitioning" to linkedPartitioning,
        )
    ).execute(api).mapToError()

    suspend fun getUserPlaylists(
        showTracks: Boolean = true,
        linkedPartitioning: Boolean = true,
        limit: Int = 50,
    ) = ApiRequest.get<Playlists>(
        listOf(
            "me",
            "playlists",
        ),
        queryParameters = listOf(
            "show_tracks" to showTracks,
            "linked_partitioning" to linkedPartitioning,
            "limit" to limit,
        )
    ).execute(api).mapToError()

    suspend fun getUserTracks(
        limit: Int = 50,
        linkedPartitioning: Boolean = true,
    ) = ApiRequest.get<Tracks>(
        listOf(
            "me",
            "tracks",
        ),
        queryParameters = listOf(
            "limit" to limit,
            "linked_partitioning" to linkedPartitioning,
        )
    ).execute(api).mapToError()

    suspend fun getPlaylist(
        playlistId: Int,
        secretToken: String? = null,
        access: Set<Access> = setOf(Access.PLAYABLE, Access.PREVIEW),
        showTracks: Boolean = true,
    ) = ApiRequest.get<Track>(
        listOf(
            "playlists",
            "$playlistId",
        ),
        queryParameters = listOf(
            "secret_token" to secretToken,
            "access_token" to access.joinToString(",") { it.value },
            "show_tracks" to showTracks,
        )
    ).execute(api).mapToError()

    suspend fun deletePlaylist(
        playlistId: Int,
    ) = ApiRequest.delete<Unit>(
        listOf(
            "playlists",
            "$playlistId",
        ),
    ).execute(api).mapToError()

    suspend fun getTrack(
        trackId: Int,
        secretToken: String? = null,
    ) = ApiRequest.get<Track>(
        listOf(
            "tracks",
            "$trackId",
        ),
        queryParameters = listOf(
            "secret_token" to secretToken,
        )
    ).execute(api).mapToError()

    suspend fun getTrackStreams(
        trackId: Int,
        secretToken: String? = null,
    ) = ApiRequest.get<Streams>(
        listOf(
            "tracks",
            "$trackId",
            "streams",
        ),
        queryParameters = listOf(
            "secret_token" to secretToken,
        )
    ).execute(api).mapToError()

    suspend fun getUser(
        userId: Int,
    ) = ApiRequest.get<User>(
        listOf(
            "users",
            "$userId",
        ),
    ).execute(api).mapToError()

    suspend fun getUserPlaylists(
        userId: Int,
        access: Set<Access> = setOf(Access.PLAYABLE, Access.PREVIEW),
        showTracks: Boolean = true,
        limit: Int = 50,
        linkedPartitioning: Boolean = true,
    ) = ApiRequest.get<Playlists>(
        listOf(
            "users",
            "$userId",
            "playlists",
        ),
        queryParameters = listOf(
            "access_token" to access.joinToString(",") { it.value },
            "show_tracks" to showTracks,
            "limit" to limit,
            "linked_partitioning" to linkedPartitioning,
        )
    ).execute(api).mapToError()

    suspend fun getUserTracks(
        userId: Int,
        access: Set<Access> = setOf(Access.PLAYABLE, Access.PREVIEW),
        limit: Int = 50,
        linkedPartitioning: Boolean = true,
    ) = ApiRequest.get<Tracks>(
        listOf(
            "users",
            "$userId",
            "tracks",
        ),
        queryParameters = listOf(
            "access_token" to access.joinToString(",") { it.value },
            "limit" to limit,
            "linked_partitioning" to linkedPartitioning,
        )
    ).execute(api).mapToError()

    suspend fun likeTrack(
        trackId: Int,
    ) = ApiRequest.post<Any, Unit>(
        listOf(
            "likes",
            "tracks",
            "$trackId",
        ),
    ).execute(api).mapToError()

    suspend fun unlikeTrack(
        trackId: Int,
    ) = ApiRequest.delete<Unit>(
        listOf(
            "likes",
            "tracks",
            "$trackId",
        ),
    ).execute(api).mapToError()

    companion object {
        val serverUri: Uri = Uri.Builder()
            .scheme("https")
            .authority("api.soundcloud.com")
            .build()
    }
}
