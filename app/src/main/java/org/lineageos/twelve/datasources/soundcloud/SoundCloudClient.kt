/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud

import android.net.Uri
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.lineageos.twelve.datasources.soundcloud.models.Access
import org.lineageos.twelve.datasources.soundcloud.models.PaginatedCollection
import org.lineageos.twelve.datasources.soundcloud.models.Streams
import org.lineageos.twelve.datasources.soundcloud.models.Track
import org.lineageos.twelve.datasources.soundcloud.models.TranscodingDirectUrl
import org.lineageos.twelve.datasources.soundcloud.models.User
import org.lineageos.twelve.models.Result.Companion.map
import org.lineageos.twelve.utils.Api
import org.lineageos.twelve.utils.ApiRequest
import org.lineageos.twelve.utils.mapToError
import org.lineageos.twelve.datasources.soundcloud.models.Set as SoundCloudSet

/**
 * SoundCloud client.
 *
 * [API reference](https://developers.soundcloud.com/docs/api/explorer/open-api)
 *
 * @param cache OkHttp's [Cache]
 */
class SoundCloudClient(
    clientId: String? = null,
    private val cache: Cache? = null,
) {
    private val authInterceptor = SoundCloudAuthInterceptor(clientId)

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
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
    ) = ApiRequest.get<PaginatedCollection<Int>>(
        listOf(
            "me",
            "track_likes",
            "ids",
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
    ) = ApiRequest.get<PaginatedCollection<SoundCloudSet>>(
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
    ) = ApiRequest.get<PaginatedCollection<SoundCloudSet>>(
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
    ) = ApiRequest.get<PaginatedCollection<Track>>(
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
    ) = ApiRequest.get<SoundCloudSet>(
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

    suspend fun getTracks(
        trackIds: List<Int>,
    ) = ApiRequest.get<List<Track>>(
        listOf(
            "tracks",
        ),
        queryParameters = listOf(
            "ids" to trackIds.joinToString(",")
        )
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

    suspend fun getTrackTranscodingDirectUrl(
        url: Uri,
    ) = ApiRequest.get<TranscodingDirectUrl>(
        url.pathSegments
    ).execute(api).mapToError().map {
        addAuthParameters(it.url)
    }

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

    suspend fun getUserAlbums(
        userId: Int,
        limit: Int = 50,
        offset: Int = 0,
        linkedPartitioning: Boolean = true,
    ) = ApiRequest.get<PaginatedCollection<SoundCloudSet>>(
        listOf(
            "users",
            "$userId",
            "albums",
        ),
        queryParameters = listOf(
            "limit" to limit,
            "offset" to offset,
            "linked_partitioning" to linkedPartitioning,
        )
    ).execute(api).mapToError()

    suspend fun getUserPlaylists(
        userId: Int,
        access: Set<Access> = setOf(Access.PLAYABLE, Access.PREVIEW),
        showTracks: Boolean = true,
        limit: Int = 50,
        linkedPartitioning: Boolean = true,
    ) = ApiRequest.get<PaginatedCollection<SoundCloudSet>>(
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
    ) = ApiRequest.get<PaginatedCollection<Track>>(
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

    fun addAuthParameters(uri: Uri): Uri = uri.buildUpon()
        .apply {
            authInterceptor.getAuthParameters().forEach { (key, value) ->
                appendQueryParameter(key, value)
            }
        }
        .build()

    companion object {
        private const val AUTHORITY = "api-v2.soundcloud.com"

        val serverUri: Uri = Uri.Builder()
            .scheme("https")
            .authority(AUTHORITY)
            .build()
    }
}
