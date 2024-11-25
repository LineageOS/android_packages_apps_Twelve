/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.jellyfin

import android.net.Uri
import kotlinx.serialization.json.Json
import okhttp3.Headers
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
 * @param apiKey The API key to use
 */
class JellyfinClient(
    server: String,
    private val apiKey: String,
) {
    private val okHttpClient = OkHttpClient()

    private val serverUri = Uri.parse(server)

    suspend fun getAlbums(sortingRule: SortingRule) = method<BaseItemDtoQueryResult>(
        listOf("Items"),
        sortingRule,
        listOf(
            "IncludeItemTypes" to "MusicAlbum",
            "Recursive" to true
        )
    )

    suspend fun getArtists(sortingRule: SortingRule) = method<BaseItemDtoQueryResult>(
        listOf("Artists"),
        sortingRule,
        listOf(
            "Recursive" to true
        )
    )

    suspend fun getGenres(sortingRule: SortingRule) = method<BaseItemDtoQueryResult>(
        listOf("Genres"),
        sortingRule,
        listOf(
            "Recursive" to true
        )
    )

    suspend fun getPlaylists(sortingRule: SortingRule) = method<BaseItemDtoQueryResult>(
        listOf("Items"),
        sortingRule,
        listOf(
            "IncludeItemTypes" to "Playlist",
            "Recursive" to true
        )
    )

    suspend fun getItems(query: String) = method<BaseItemDtoQueryResult>(
        listOf("Items"),
        null,
        listOf(
            "SearchTerm" to query,
            "IncludeItemTypes" to "Playlist,MusicAlbum,MusicArtist,MusicGenre",
            "Recursive" to true,
        )
    )

    suspend fun getAlbum(id: UUID) = getItem(id)

    suspend fun getArtist(id: UUID) = getItem(id)

    fun getAlbumThumbnail(id: UUID) = getItemThumbnail(id)

    fun getArtistThumbnail(id: UUID) = getItemThumbnail(id)

    suspend fun getAlbumTracks(id: UUID) = method<BaseItemDtoQueryResult>(
        listOf("Items"),
        null,
        listOf(
            "ParentId" to id,
        )
    )

    suspend fun getArtistWorks(id: UUID) = method<BaseItemDtoQueryResult>(
        listOf("Items"),
        null,
        listOf(
            "ArtistIds" to id,
            "IncludeItemTypes" to "MusicAlbum",
            "Recursive" to true,
        )
    )

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
        null,
    )

    private fun getItemThumbnail(id: UUID) = getMethodUrl(
        listOf(
            "Items",
            id.toString(),
            "Images",
            "Primary",
            "0",
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
            .headers(getBaseHeaders())
            .build()
    ).executeAsync().let { response ->
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
    }.build().toString().also {
        println("URL: $it")
    }

    private fun getBaseHeaders() = Headers.Builder().apply {
        add("Authorization", "MediaBrowser Token=\"${apiKey}\"")
    }.build()

    private fun getSortParameter(sortingRule: SortingRule) = buildMap {
        put(
            "sortBy", when (sortingRule.strategy) {
                SortingStrategy.CREATION_DATE -> "DateCreated"
                SortingStrategy.MODIFICATION_DATE -> "DateLastContentAdded"
                SortingStrategy.NAME -> "Name"
                SortingStrategy.PLAY_COUNT -> "Default" // API says it's supported, it doesn't work
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
