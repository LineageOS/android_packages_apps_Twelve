/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources

import android.net.Uri
import android.os.Bundle
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import okhttp3.Cache
import org.lineageos.twelve.R
import org.lineageos.twelve.datasources.soundcloud.SoundCloudClient
import org.lineageos.twelve.datasources.soundcloud.models.Track
import org.lineageos.twelve.models.ActivityTab
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.ArtistWorks
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.DataSourceInformation
import org.lineageos.twelve.models.Error
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.GenreContent
import org.lineageos.twelve.models.LocalizedString
import org.lineageos.twelve.models.Lyrics
import org.lineageos.twelve.models.MediaItem
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.ProviderArgument
import org.lineageos.twelve.models.ProviderArgument.Companion.requireArgument
import org.lineageos.twelve.models.Result
import org.lineageos.twelve.models.Result.Companion.map
import org.lineageos.twelve.models.SortingRule

/**
 * SoundCloud backed data source.
 */
class SoundCloudDataSource(
    arguments: Bundle,
    cache: Cache? = null,
) : MediaDataSource {
    private val clientId = arguments.requireArgument(ARG_CLIENT_ID)

    private val soundCloudClient = SoundCloudClient(clientId, cache)

    private val albumsUri = SoundCloudClient.serverUri.buildUpon()
        .appendPath(ALBUMS_PATH)
        .build()
    private val artistsUri = SoundCloudClient.serverUri.buildUpon()
        .appendPath(ARTISTS_PATH)
        .build()
    private val audiosUri = SoundCloudClient.serverUri.buildUpon()
        .appendPath(AUDIOS_PATH)
        .build()
    private val genresUri = SoundCloudClient.serverUri.buildUpon()
        .appendPath(GENRES_PATH)
        .build()
    private val playlistsUri = SoundCloudClient.serverUri.buildUpon()
        .appendPath(PLAYLISTS_PATH)
        .build()

    private val favoritesUri = SoundCloudClient.serverUri.buildUpon()
        .appendPath(FAVORITES_PATH)
        .build()

    override fun status() = suspend {
        val me = soundCloudClient.me()

        me.map {
            listOf(
                DataSourceInformation(
                    "user",
                    LocalizedString.StringLocalizedString("user"),
                    LocalizedString.StringLocalizedString(it.toString()),
                )
            )
        }
    }.asFlow()

    override suspend fun mediaTypeOf(mediaItemUri: Uri) = null

    override fun activity() = flowOf(
        Result.Success<_, Error>(listOf<ActivityTab>())
    )

    override fun albums(sortingRule: SortingRule) = flowOf(
        Result.Success<_, Error>(listOf<Album>())
    )

    override fun artists(sortingRule: SortingRule) = flowOf(
        Result.Success<_, Error>(listOf<Artist>())
    )

    override fun genres(sortingRule: SortingRule) = flowOf(
        Result.Success<_, Error>(listOf<Genre>())
    )

    override fun playlists(sortingRule: SortingRule) = flowOf(
        Result.Success<_, Error>(listOf<Playlist>())
    )

    override fun search(query: String) = flowOf(
        Result.Success<_, Error>(listOf<MediaItem<*>>())
    )

    override fun audio(audioUri: Uri) = suspend {
        soundCloudClient.getTrack(
            audioUri.lastPathSegment!!.toInt(),
        )
    }.asFlow()

    override fun album(albumUri: Uri) = flowOf(
        Result.Error<Pair<Album, List<Audio>>, _>(Error.NOT_FOUND)
    )

    override fun artist(artistUri: Uri) = flowOf(
        Result.Error<Pair<Artist, ArtistWorks>, _>(Error.NOT_FOUND)
    )

    override fun genre(genreUri: Uri) = flowOf(
        Result.Error<Pair<Genre, GenreContent>, _>(Error.NOT_FOUND)
    )

    override fun playlist(playlistUri: Uri) = flowOf(
        Result.Error<Pair<Playlist, List<Audio>>, _>(Error.NOT_FOUND)
    )

    override fun audioPlaylistsStatus(audioUri: Uri) = flowOf(
        Result.Error<List<Pair<Playlist, Boolean>>, _>(Error.NOT_FOUND)
    )

    override fun lyrics(audioUri: Uri) = flowOf(
        Result.Error<Lyrics, _>(Error.NOT_FOUND)
    )

    override suspend fun createPlaylist(name: String) =
        Result.Error<Uri, _>(Error.NOT_IMPLEMENTED)

    override suspend fun renamePlaylist(playlistUri: Uri, name: String) =
        Result.Error<Unit, _>(Error.NOT_IMPLEMENTED)

    override suspend fun deletePlaylist(playlistUri: Uri) =
        Result.Error<Unit, _>(Error.NOT_IMPLEMENTED)

    override suspend fun addAudioToPlaylist(
        playlistUri: Uri,
        audioUri: Uri
    ) = Result.Error<Unit, _>(Error.NOT_IMPLEMENTED)

    override suspend fun removeAudioFromPlaylist(
        playlistUri: Uri,
        audioUri: Uri
    ) = Result.Error<Unit, _>(Error.NOT_IMPLEMENTED)

    override suspend fun onAudioPlayed(audioUri: Uri) = Result.Success<_, Error>(Unit)

    override suspend fun setFavorite(
        audioUri: Uri,
        isFavorite: Boolean
    ) = Result.Error<Unit, _>(Error.NOT_IMPLEMENTED)

    private fun Track.toModel() = Audio.Builder()

    companion object {
        private const val ALBUMS_PATH = "albums"
        private const val ARTISTS_PATH = "artists"
        private const val AUDIOS_PATH = "audio"
        private const val GENRES_PATH = "genres"
        private const val PLAYLISTS_PATH = "playlists"

        private const val FAVORITES_PATH = "favorites"

        val ARG_CLIENT_ID = ProviderArgument(
            "client_id",
            String::class,
            R.string.provider_argument_client_id,
            required = true,
            hidden = false,
        )
    }
}
