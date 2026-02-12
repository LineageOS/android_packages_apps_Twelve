/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache

import androidx.core.net.toUri
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.lineageos.twelve.datasources.ampache.models.Album
import org.lineageos.twelve.datasources.ampache.models.Albums
import org.lineageos.twelve.datasources.ampache.models.Artist
import org.lineageos.twelve.datasources.ampache.models.Artists
import org.lineageos.twelve.datasources.ampache.models.Genre
import org.lineageos.twelve.datasources.ampache.models.Genres
import org.lineageos.twelve.datasources.ampache.models.Ping
import org.lineageos.twelve.datasources.ampache.models.Playlist
import org.lineageos.twelve.datasources.ampache.models.Playlists
import org.lineageos.twelve.datasources.ampache.models.Preferences
import org.lineageos.twelve.datasources.ampache.models.Song
import org.lineageos.twelve.datasources.ampache.models.Songs
import org.lineageos.twelve.datasources.ampache.models.Success
import org.lineageos.twelve.models.Error
import org.lineageos.twelve.models.Result
import org.lineageos.twelve.utils.Api
import org.lineageos.twelve.utils.ApiRequest
import org.lineageos.twelve.utils.mapToError
import java.security.MessageDigest
import java.time.Instant
import kotlin.reflect.KMutableProperty0

/**
 * Ampache client.
 *
 * Compliant with API6.
 */
class AmpacheClient(
    server: String,
    username: String,
    password: String,
    applicationName: String,
    tokenProperty: KMutableProperty0<Pair<String, Instant>?>,
    cache: Cache? = null,
) {
    private val interceptor = AmpacheInterceptor(username, password, applicationName, tokenProperty)

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .cache(cache)
        .build()

    private val serverUri = server.toUri().buildUpon()
        .appendPath("server")
        .appendPath("json.server.php")
        .build()

    private val api = Api(okHttpClient, serverUri)

    /**
     * This can be called without being authenticated, it is useful for determining if what the
     * status of the server is, and what version it is running/compatible with.
     */
    suspend fun ping() = action<Ping>("ping")

    /**
     * Check Ampache for updates and run the update if there is one.
     */
    suspend fun systemUpdate() = action<Success>("system_update")

    /**
     * Get your server preferences.
     *
     * Access required: 100 (Admin)
     */
    suspend fun systemPreferences() = action<Preferences>("system_preferences")

    /**
     * Get your user preferences.
     */
    suspend fun userPreferences() = action<Preferences>("user_preferences")

    /**
     * This returns albums based on the provided search filters.
     *
     * @param filter Filter results to match this string
     * @param includeAlbums Whether to include albums in the response
     * @param includeSongs Whether to include songs in the response
     * @param exact if true filter is exact = rather than fuzzy LIKE
     * @param add ISO 8601 Date Format (2020-09-16) Find objects with an 'add' date newer than the
     *            specified date
     * @param update ISO 8601 Date Format (2020-09-16) Find objects with an 'update' time newer than
     *               the specified date
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun albums(
        filter: String? = null,
        includeAlbums: Boolean = false,
        includeSongs: Boolean = false,
        exact: Boolean = false,
        add: String? = null,
        update: String? = null,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Albums>(
        "albums",
        listOf(
            "filter" to filter,
            "include" to when {
                includeAlbums && includeSongs -> "albums,songs"
                includeAlbums -> "albums"
                includeSongs -> "songs"
                else -> null
            },
            "exact" to exact,
            "add" to add,
            "update" to update,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * This returns a single album based on the UID provided.
     *
     * @param filter UID of Album, returns album JSON
     * @param includeSongs Whether to include songs in the response
     */
    suspend fun album(
        filter: String,
        includeSongs: Boolean = false,
    ) = action<Album>(
        "album",
        listOf(
            "filter" to filter,
            "include" to when {
                includeSongs -> "songs"
                else -> null
            },
        ),
    )

    /**
     * This returns the songs of a specified album.
     *
     * @param filter UID of Album, returns song JSON
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated
     *             comma string pairs (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun albumSongs(
        filter: String,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Songs>(
        "album_songs",
        listOf(
            "filter" to filter,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * This takes a collection of inputs and returns artist objects.
     *
     * @param filter Filter results to match this string
     * @param exact if true filter is exact = rather than fuzzy LIKE
     * @param add ISO 8601 Date Format (2020-09-16) Find objects with an 'add' date newer than the
     *            specified date
     * @param update ISO 8601 Date Format (2020-09-16) Find objects with an 'update' time newer than
     *               the specified date
     * @param includeAlbums Whether to include albums in the response
     * @param includeSongs Whether to include songs in the response
     * @param albumArtist if true filter for album artists only
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun artists(
        filter: String? = null,
        exact: Boolean? = null,
        add: String? = null,
        update: String? = null,
        includeAlbums: Boolean = false,
        includeSongs: Boolean = false,
        albumArtist: Boolean? = null,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Artists>(
        "artists",
        listOf(
            "filter" to filter,
            "exact" to exact,
            "add" to add,
            "update" to update,
            "include" to when {
                includeAlbums && includeSongs -> "albums,songs"
                includeAlbums -> "albums"
                includeSongs -> "songs"
                else -> null
            },
            "album_artist" to albumArtist,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * This returns a single artist based on the UID of said artist.
     *
     * @param filter UID of Artist, returns artist JSON
     * @param includeAlbums Whether to include albums in the response
     * @param includeSongs Whether to include songs in the response
     */
    suspend fun artist(
        filter: String,
        includeAlbums: Boolean = false,
        includeSongs: Boolean = false,
    ) = action<Artist>(
        "artist",
        listOf(
            "filter" to filter,
            "include" to when {
                includeAlbums && includeSongs -> "albums,songs"
                includeAlbums -> "albums"
                includeSongs -> "songs"
                else -> null
            }
        ),
    )

    /**
     * This returns the genres (Tags) based on the specified filter
     *
     * @param filter Filter results to match this string
     * @param exact if true filter is exact = rather than fuzzy LIKE
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun genres(
        filter: String? = null,
        exact: Boolean? = null,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Genres>(
        "genres",
        listOf(
            "filter" to filter,
            "exact" to exact,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * This returns a single genre based on UID.
     *
     * @param filter UID of genre, returns genre JSON
     */
    suspend fun genre(
        filter: String,
    ) = action<Genre>(
        "genre",
        listOf(
            "filter" to filter,
        ),
    )

    /**
     * This returns the albums associated with the genre in question.
     *
     * @param filter UID of genre, returns album JSON
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun genreAlbums(
        filter: String,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Albums>(
        "genre_albums",
        listOf(
            "filter" to filter,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * This returns the artists associated with the genre in question as defined by the UID.
     *
     * @param filter UID of genre, returns artist JSON
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun genreArtists(
        filter: String,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Artists>(
        "genre_artists",
        listOf(
            "filter" to filter,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * Returns the songs for this genre.
     *
     * @param filter UID of genre, returns song JSON
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun genreSongs(
        filter: String,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Songs>(
        "genre_songs",
        listOf(
            "filter" to filter,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * This returns playlists based on the specified filter.
     *
     * @param filter Filter results to match this string
     * @param hideSearch if true do not include searches/smartlists in the result
     * @param showDupes if true if true ignore 'api_hide_dupe_searches' setting
     * @param exact if true filter is exact = rather than fuzzy LIKE
     * @param add ISO 8601 Date Format (2020-09-16) Find objects with an 'add' date newer than the
     *            specified date
     * @param update ISO 8601 Date Format (2020-09-16) Find objects with an 'update' time newer than
     *               the specified date
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun playlists(
        filter: String? = null,
        hideSearch: Boolean? = null,
        showDupes: Boolean? = null,
        exact: Boolean? = null,
        add: String? = null,
        update: String? = null,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Playlists>(
        "playlists",
        listOf(
            "filter" to filter,
            "hide_search" to hideSearch,
            "show_dupes" to showDupes,
            "exact" to exact,
            "add" to add,
            "update" to update,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * This returns a single playlist.
     *
     * @param filter UID of playlist, returns playlist JSON
     */
    suspend fun playlist(
        filter: String,
    ) = action<Playlist>(
        "playlist",
        listOf(
            "filter" to filter,
        ),
    )

    /**
     * This returns the songs for a playlist.
     *
     * @param filter UID of Playlist, returns song JSON
     * @param random if true get random songs using limit
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     */
    suspend fun playlistSongs(
        filter: String,
        random: Boolean? = null,
        offset: Int? = null,
        limit: Int? = null,
    ) = action<Songs>(
        "playlist_songs",
        listOf(
            "filter" to filter,
            "random" to random,
            "offset" to offset,
            "limit" to limit,
        ),
    )

    /**
     * Returns songs based on the specified filter.
     *
     * @param filter Filter results to match this string
     * @param exact if true filter is exact = rather than fuzzy LIKE
     * @param add ISO 8601 Date Format (2020-09-16) Find objects with an 'add' date newer than the
     *            specified date
     * @param update ISO 8601 Date Format (2020-09-16) Find objects with an 'update' time newer than
     *               the specified date
     * @param offset Return results starting from this index position
     * @param limit Maximum number of results to return
     * @param cond Apply additional filters to the browse using ; separated comma string pairs
     *             (e.g. 'filter1,value1;filter2,value2')
     * @param sort Sort name or comma-separated key pair. (e.g. 'name,order')
     *             Default order 'ASC' (e.g. 'name,ASC' == 'name')
     */
    suspend fun songs(
        filter: String? = null,
        exact: Boolean? = null,
        add: String? = null,
        update: String? = null,
        offset: Int? = null,
        limit: Int? = null,
        cond: String? = null,
        sort: String? = null,
    ) = action<Songs>(
        "songs",
        listOf(
            "filter" to filter,
            "exact" to exact,
            "add" to add,
            "update" to update,
            "offset" to offset,
            "limit" to limit,
            "cond" to cond,
            "sort" to sort,
        ),
    )

    /**
     * Returns a single song.
     *
     * @param filter UID of Song, returns song JSON
     */
    suspend fun song(
        filter: String,
    ) = action<Song>(
        "song",
        listOf(
            "filter" to filter,
        ),
    )

    private suspend inline fun <reified T> action(
        action: String,
        parameters: List<Pair<String, Any?>> = emptyList(),
    ): Result<T, Error> = ApiRequest.get<T>(
        listOf(),
        listOf(
            "action" to action,
        ) + parameters
    ).execute(api).mapToError()

    companion object {
        const val API_VERSION = "6.0.0"

        private val messageDigest by lazy { MessageDigest.getInstance("SHA-256") }

        @OptIn(ExperimentalStdlibApi::class)
        fun calculatePassphrase(
            password: String,
            instant: Instant,
        ): String {
            val time = instant.epochSecond
            val key = messageDigest.digest(password.toByteArray()).toHexString()
            return messageDigest.digest((time.toString() + key).toByteArray()).toHexString()
        }
    }
}
