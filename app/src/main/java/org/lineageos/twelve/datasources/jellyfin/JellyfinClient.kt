/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.jellyfin

import android.net.Uri
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.lineageos.twelve.datasources.jellyfin.models.CreatePlaylist
import org.lineageos.twelve.datasources.jellyfin.models.CreatePlaylistResult
import org.lineageos.twelve.datasources.jellyfin.models.Item
import org.lineageos.twelve.datasources.jellyfin.models.PlaylistItems
import org.lineageos.twelve.datasources.jellyfin.models.QueryResult
import org.lineageos.twelve.datasources.jellyfin.models.UpdatePlaylist
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.SortingStrategy
import org.lineageos.twelve.utils.Api
import java.util.UUID

/**
 * Jellyfin client.
 *
 * @param server The base URL of the server
 * @param username The login username of the server
 * @param password The corresponding password of the user
 * @param deviceIdentifier The device identifier
 * @param packageName The package name of the app
 * @param tokenGetter A function to get the token
 * @param tokenSetter A function to set the token
 * @param cache OkHttp's [Cache]
 */
class JellyfinClient(
    server: String,
    private val username: String,
    private val password: String,
    private val deviceIdentifier: String,
    private val packageName: String,
    tokenGetter: () -> String?,
    tokenSetter: (String) -> Unit,
    cache: Cache? = null,
) {
    private val serverUri = Uri.parse(server)

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(JellyfinAuthInterceptor(tokenGetter))
        .authenticator(
            JellyfinAuthenticator(
                serverUri,
                username,
                password,
                deviceIdentifier,
                packageName,
                tokenGetter,
                tokenSetter,
            )
        )
        .cache(cache)
        .build()

    private val api = Api(okHttpClient, serverUri)

    suspend fun getAlbums(sortingRule: SortingRule) = api.get<QueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "IncludeItemTypes" to "MusicAlbum",
            "Recursive" to true,
        ) + getSortParameter(sortingRule),
    )

    suspend fun getArtists(sortingRule: SortingRule) = api.get<QueryResult>(
        listOf("Artists"),
        queryParameters = listOf(
            "IncludeItemTypes" to "Audio",
            "Recursive" to true,
        ) + getSortParameter(sortingRule),
    )

    suspend fun getGenres(sortingRule: SortingRule) = api.get<QueryResult>(
        listOf("Genres"),
        queryParameters = listOf(
            "IncludeItemTypes" to "Audio",
            "Recursive" to true,
        ) + getSortParameter(sortingRule),
    )

    suspend fun getPlaylists(sortingRule: SortingRule) = api.get<QueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "IncludeItemTypes" to "Playlist",
            "Recursive" to true,
        ) + getSortParameter(sortingRule),
    )

    suspend fun getItems(query: String) = api.get<QueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "SearchTerm" to query,
            "IncludeItemTypes" to "Playlist,MusicAlbum,MusicArtist,MusicGenre,Audio",
            "Recursive" to true,
        ),
    )

    suspend fun getAlbum(id: UUID) = getItem(id)

    suspend fun getArtist(id: UUID) = getItem(id)

    suspend fun getPlaylist(id: UUID) = getItem(id)

    suspend fun getGenre(id: UUID) = getItem(id)

    fun getAlbumThumbnail(id: UUID) = getItemThumbnail(id)

    fun getArtistThumbnail(id: UUID) = getItemThumbnail(id)

    fun getPlaylistThumbnail(id: UUID) = getItemThumbnail(id)

    fun getGenreThumbnail(id: UUID) = getItemThumbnail(id)

    suspend fun getAlbumTracks(id: UUID) = api.get<QueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "ParentId" to id,
            "IncludeItemTypes" to "Audio",
            "Recursive" to true,
        ),
    )

    suspend fun getArtistWorks(id: UUID) = api.get<QueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "ArtistIds" to id,
            "IncludeItemTypes" to "MusicAlbum",
            "Recursive" to true,
        ),
    )

    suspend fun getPlaylistItemIds(id: UUID) = api.get<PlaylistItems>(
        listOf(
            "Playlists",
            id.toString(),
        ),
    )

    suspend fun getPlaylistTracks(id: UUID) = api.get<QueryResult>(
        listOf(
            "Playlists",
            id.toString(),
            "Items",
        ),
    )

    suspend fun getGenreContent(id: UUID) = api.get<QueryResult>(
        listOf("Items"),
        queryParameters = listOf(
            "GenreIds" to id,
            "IncludeItemTypes" to "MusicAlbum,Playlist,Audio",
            "Recursive" to true,
        ),
    )

    suspend fun getAudio(id: UUID) = getItem(id)

    fun getAudioPlaybackUrl(id: UUID) = api.buildUrl(
        listOf(
            "Audio",
            id.toString(),
            "stream",
        ),
        queryParameters = listOf(
            "static" to true,
        ),
    )

    suspend fun createPlaylist(name: String) = api.post<CreatePlaylist, CreatePlaylistResult>(
        listOf("Playlists"),
        data = CreatePlaylist(
            name = name,
            ids = listOf(),
            users = listOf(),
            isPublic = true,
        ),
    )

    suspend fun renamePlaylist(id: UUID, name: String) = api.post<UpdatePlaylist, Unit>(
        listOf(
            "Playlists",
            id.toString(),
        ),
        data = UpdatePlaylist(
            name = name,
        ),
    )

    suspend fun addItemToPlaylist(id: UUID, audioId: UUID) = api.post<Any, Unit>(
        listOf(
            "Playlists",
            id.toString(),
            "Items",
        ),
        queryParameters = listOf(
            "Ids" to audioId,
        ),
    )

    suspend fun removeItemFromPlaylist(id: UUID, audioId: UUID) = api.delete<Unit>(
        listOf(
            "Playlists",
            id.toString(),
            "Items",
        ),
        queryParameters = listOf(
            "EntryIds" to audioId,
        ),
    )

    private suspend fun getItem(id: UUID) = api.get<Item>(
        listOf(
            "Items",
            id.toString(),
        ),
    )

    private fun getItemThumbnail(id: UUID) = api.buildUrl(
        listOf(
            "Items",
            id.toString(),
            "Images",
            "Primary",
        ),
    )

    private fun getSortParameter(sortingRule: SortingRule) = buildList {
        add(
            "sortBy" to when (sortingRule.strategy) {
                SortingStrategy.CREATION_DATE -> "DateCreated"
                SortingStrategy.MODIFICATION_DATE -> "DateLastContentAdded"
                SortingStrategy.NAME -> "Name"
                SortingStrategy.PLAY_COUNT -> "PlayCount"
            }
        )

        add(
            "sortOrder" to when (sortingRule.reverse) {
                true -> "Descending"
                false -> "Ascending"
            }
        )
    }

    companion object {
        const val JELLYFIN_API_VERSION = "10.10.3"
    }
}
