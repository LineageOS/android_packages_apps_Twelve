/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.jellyfin

import android.net.Uri
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemDtoQueryResult
import org.lineageos.twelve.ext.executeAsync
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.SortingStrategy

/**
 * Jellyfin client.
 *
 * @param server The base URL of the server
 * @param cache The http cache
 * @param username The login username of the server
 * @param password The corresponding password of the user
 * @param deviceIdentifier The device identifier
 * @param packageName The package name of the app
 */
class JellyfinClient(
    server: String,
    private val cache: Cache,
    private val username: String,
    private val password: String,
    private val deviceIdentifier: String,
    private val packageName: String
) {
    private val serverUri = Uri.parse(server)

    private val authenticationMutex = Mutex()
    private val authenticator = JellyfinAuthenticator(
        authenticationMutex,
        serverUri,
        username,
        password,
        deviceIdentifier,
        packageName
    )

    private val okHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .authenticator(authenticator)
        .build()

    suspend fun getAlbums(sortingRule: SortingRule) = method<BaseItemDtoQueryResult>(
        listOf("Items"),
        sortingRule,
        listOf(
            "IncludeItemTypes" to "MusicAlbum",
            "Recursive" to true,
        )
    )

    suspend fun getArtists(sortingRule: SortingRule) = method<BaseItemDtoQueryResult>(
        listOf("Artists"),
        sortingRule,
        listOf(
            "Recursive" to true,
        )
    )

    suspend fun getGenres(sortingRule: SortingRule) = method<BaseItemDtoQueryResult>(
        listOf("Genres"),
        sortingRule,
        listOf(
            "Recursive" to true,
        )
    )

    suspend fun getPlaylists(sortingRule: SortingRule) = method<BaseItemDtoQueryResult>(
        listOf("Items"),
        sortingRule,
        listOf(
            "IncludeItemTypes" to "Playlist",
            "Recursive" to true,
        )
    )

    suspend fun getItems(query: String) = method<BaseItemDtoQueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "SearchTerm" to query,
            "IncludeItemTypes" to "Playlist,MusicAlbum,MusicArtist,MusicGenre",
            "Recursive" to true,
        )
    )

    suspend fun getAlbum(id: UUID) = getItem(id)

    suspend fun getArtist(id: UUID) = getItem(id)

    suspend fun getPlaylist(id: UUID) = getItem(id)

    suspend fun getGenre(id: UUID) = getItem(id)

    fun getAlbumThumbnail(id: UUID) = getItemThumbnail(id)

    fun getArtistThumbnail(id: UUID) = getItemThumbnail(id)

    fun getPlaylistThumbnail(id: UUID) = getItemThumbnail(id)

    fun getGenreThumbnail(id: UUID) = getItemThumbnail(id)

    suspend fun getAlbumTracks(id: UUID) = method<BaseItemDtoQueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "ParentId" to id,
        )
    )

    suspend fun getArtistWorks(id: UUID) = method<BaseItemDtoQueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "ArtistIds" to id,
            "IncludeItemTypes" to "MusicAlbum",
            "Recursive" to true,
        )
    )

    suspend fun getPlaylistTracks(id: UUID) = method<BaseItemDtoQueryResult>(
        listOf(
            "Playlists",
            id.toString(),
            "Items",
        ),
    )

    suspend fun getGenreContent(id: UUID) = method<BaseItemDtoQueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "GenreIds" to id,
            "IncludeItemTypes" to "MusicAlbum,Playlist,Audio",
            "Recursive" to true,
        )
    )

    suspend fun getAudio(id: UUID) = getItem(id)

    fun getAudioPlaybackUrl(id: UUID) = getMethodUrl(
        listOf(
            "Audio",
            id.toString(),
            "stream",
        ),
        queryParameters = listOf(
            "static" to true,
        )
    )

    private suspend fun getItem(id: UUID) = method<BaseItemDto>(
        listOf("Items", id.toString()),
    )

    private fun getItemThumbnail(id: UUID) = getMethodUrl(
        listOf(
            "Items",
            id.toString(),
            "Images",
            "Primary",
        )
    )

    private suspend inline fun <reified T> method(
        method: List<String>,
        sortingRule: SortingRule? = null,
        queryParameters: List<Pair<String, Any?>> = emptyList(),
    ) = okHttpClient.newCall(
        Request
            .Builder()
            .url(getMethodUrl(method, sortingRule, queryParameters))
            .build()
    ).executeAsync().let { response ->
        println("URL: ${response.request.url}, code: ${response.code}")

        when (response.isSuccessful) {
            true -> response.body?.use { body ->
                body.string().let {
                    MethodResult.Success(Json.decodeFromString<T>(it))
                }
            } ?: throw Exception("Successful response with empty body")

            false -> MethodResult.HttpError(response.code)
        }
    }

    private fun getMethodUrl(
        method: List<String>,
        sortingRule: SortingRule? = null,
        queryParameters: List<Pair<String, Any?>> = emptyList()
    ) = serverUri.buildUpon().apply {
        method.forEach {
            appendPath(it)
        }
        queryParameters.forEach { (key, value) ->
            value?.let { appendQueryParameter(key, it.toString()) }
        }
        sortingRule?.let {
            getSortParameter(it).forEach { (key, value) ->
                appendQueryParameter(key, value)
            }
        }
    }.build().toString()

    private fun getSortParameter(sortingRule: SortingRule) = buildMap {
        put(
            "sortBy", when (sortingRule.strategy) {
                SortingStrategy.CREATION_DATE -> "DateCreated"
                SortingStrategy.MODIFICATION_DATE -> "DateLastContentAdded"
                SortingStrategy.NAME -> "Name"
                SortingStrategy.PLAY_COUNT -> "PlayCount"
            }
        )

        put(
            "sortOrder", when (sortingRule.reverse) {
                true -> "Descending"
                false -> "Ascending"
            }
        )
    }

    sealed interface MethodResult<T> {
        class Success<T>(val result: T) : MethodResult<T>
        class HttpError<T>(val code: Int) : MethodResult<T>
    }
}