/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.jellyfin

import android.net.Uri
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemDtoQueryResult
import org.jellyfin.sdk.model.api.CreatePlaylistDto
import org.jellyfin.sdk.model.api.PlaylistCreationResult
import org.jellyfin.sdk.model.api.UpdatePlaylistDto
import org.lineageos.twelve.ext.executeAsync
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.SortingStrategy

/**
 * Jellyfin client.
 *
 * @param server The base URL of the server
 * @param username The login username of the server
 * @param password The corresponding password of the user
 * @param deviceIdentifier The device identifier
 * @param packageName The package name of the app
 * @param cache OkHttp's [Cache]
 */
class JellyfinClient(
    server: String,
    private val username: String,
    private val password: String,
    private val deviceIdentifier: String,
    private val packageName: String,
    cache: Cache? = null,
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

    suspend fun getAlbums(sortingRule: SortingRule) = getMethod<BaseItemDtoQueryResult>(
        listOf("Items"),
        sortingRule,
        listOf(
            "IncludeItemTypes" to "MusicAlbum",
            "Recursive" to true,
        )
    )

    suspend fun getArtists(sortingRule: SortingRule) = getMethod<BaseItemDtoQueryResult>(
        listOf("Artists"),
        sortingRule,
        listOf(
            "Recursive" to true,
        )
    )

    suspend fun getGenres(sortingRule: SortingRule) = getMethod<BaseItemDtoQueryResult>(
        listOf("Genres"),
        sortingRule,
        listOf(
            "Recursive" to true,
        )
    )

    suspend fun getPlaylists(sortingRule: SortingRule) = getMethod<BaseItemDtoQueryResult>(
        listOf("Items"),
        sortingRule,
        listOf(
            "IncludeItemTypes" to "Playlist",
            "Recursive" to true,
        )
    )

    suspend fun getItems(query: String) = getMethod<BaseItemDtoQueryResult>(
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

    suspend fun getAlbumTracks(id: UUID) = getMethod<BaseItemDtoQueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "ParentId" to id,
        )
    )

    suspend fun getArtistWorks(id: UUID) = getMethod<BaseItemDtoQueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "ArtistIds" to id,
            "IncludeItemTypes" to "MusicAlbum",
            "Recursive" to true,
        )
    )

    suspend fun getPlaylistTracks(id: UUID) = getMethod<BaseItemDtoQueryResult>(
        listOf(
            "Playlists",
            id.toString(),
            "Items",
        ),
    )

    suspend fun getGenreContent(id: UUID) = getMethod<BaseItemDtoQueryResult>(
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

    suspend fun createPlaylist(name: String) =
        postMethod<CreatePlaylistDto, PlaylistCreationResult>(
            listOf("Playlists"),
            bodyParameter = CreatePlaylistDto(
                name = name,
                ids = listOf(),
                users = listOf(),
                isPublic = true,
            )
        )

    suspend fun renamePlaylist(id: UUID, name: String) = postMethod<UpdatePlaylistDto, Unit>(
        listOf(
            "Playlists",
            id.toString(),
        ),
        bodyParameter = UpdatePlaylistDto(
            name = name,
        )
    )

    suspend fun addItemToPlaylist(id: UUID, audioId: UUID) = postMethod<Any, Unit>(
        listOf(
            "Playlists",
            id.toString(),
            "Items",
        ),
        queryParameters = listOf(
            "Ids" to audioId,
        )
    )

    suspend fun removeItemFromPlaylist(id: UUID, audioId: UUID) = deleteMethod(
        listOf(
            "Playlists",
            id.toString(),
            "Items",
        ),
        queryParameters = listOf(
            "EntryIds" to audioId,
        )
    )

    private suspend fun getItem(id: UUID) = getMethod<BaseItemDto>(
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

    private suspend inline fun <reified T> getMethod(
        method: List<String>,
        sortingRule: SortingRule? = null,
        queryParameters: List<Pair<String, Any?>> = emptyList(),
    ) = okHttpClient.newCall(
        Request
            .Builder()
            .url(getMethodUrl(method, sortingRule, queryParameters))
            .build()
    ).executeAsync().let { response ->
        println("Get response: ${response.request.url}, ${response.code}")

        when (response.isSuccessful) {
            true -> response.body?.use { body ->
                body.string().let {
                    MethodResult.Success(Json.decodeFromString<T>(it))
                }
            } ?: throw Exception("Successful response with empty body")

            false -> MethodResult.HttpError(response.code)
        }
    }

    private suspend inline fun <reified T, reified E> postMethod(
        method: List<String>,
        queryParameters: List<Pair<String, Any?>> = emptyList(),
        bodyParameter: T? = null,
        emptyResponse: () -> E = { Unit as E },
    ) = okHttpClient.newCall(
        Request
            .Builder()
            .url(getMethodUrl(method, null, queryParameters))
            .post(
                bodyParameter?.let {
                    Json.encodeToString<T>(it)
                        .toRequestBody("application/json".toMediaType())
                } ?: "".toRequestBody()
            )
            .build()
    ).executeAsync().let { response ->
        println("Post response: ${response.request.url}, ${response.code}")

        when (response.isSuccessful) {
            true -> response.body?.use { body ->
                if (body.contentLength() == 0L) {
                    MethodResult.Success(emptyResponse())
                } else {
                    body.string().let {
                        MethodResult.Success(Json.decodeFromString<E>(it))
                    }
                }
            } ?: throw Exception("Successful response with empty body")

            false -> MethodResult.HttpError(response.code)
        }
    }

    private suspend inline fun deleteMethod(
        method: List<String>,
        queryParameters: List<Pair<String, Any?>> = emptyList(),
    ) = okHttpClient.newCall(
        Request
            .Builder()
            .url(getMethodUrl(method, null, queryParameters))
            .delete()
            .build()
    ).executeAsync().let { response ->
        println("Delete response: ${response.request.url}, ${response.code}")

        when (response.isSuccessful) {
            true -> MethodResult.Success(Unit)
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

    companion object {
        const val JELLYFIN_API_VERSION = "10.10.3"
    }
}
