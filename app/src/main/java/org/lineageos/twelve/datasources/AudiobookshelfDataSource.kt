/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources

import android.net.Uri
import android.os.Bundle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.lineageos.twelve.R
import org.lineageos.twelve.datasources.audiobookshelf.AudiobookshelfClient
import org.lineageos.twelve.models.ActivityTab
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.ArtistWorks
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.DataSourceInformation
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.GenreContent
import org.lineageos.twelve.models.MediaItem
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.ProviderArgument
import org.lineageos.twelve.models.ProviderArgument.Companion.requireArgument
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.Thumbnail
import org.lineageos.twelve.utils.toRequestStatus
import org.lineageos.twelve.utils.toResult

/**
 * Audiobookshelf backed data source.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AudiobookshelfDataSource(
    arguments: Bundle,
    tokenGetter: () -> String?,
    tokenSetter: (String) -> Unit,
    private val lastPlayedGetter: (String) -> Flow<Uri?>,
    private val lastPlayedSetter: suspend (String, Uri) -> Long,
    cache: Cache? = null,
) : MediaDataSource {
    private val server = arguments.requireArgument(ARG_SERVER)
    private val username = arguments.requireArgument(ARG_USERNAME)
    private val password = arguments.requireArgument(ARG_PASSWORD)

    private val client = AudiobookshelfClient(
        server, username, password, tokenGetter, tokenSetter, cache
    )

    private val dataSourceBaseUri = Uri.parse(server)

    private val albumsUri = dataSourceBaseUri.buildUpon()
        .appendPath(ALBUMS_PATH)
        .build()
    private val artistsUri = dataSourceBaseUri.buildUpon()
        .appendPath(ARTISTS_PATH)
        .build()
    private val audiosUri = dataSourceBaseUri.buildUpon()
        .appendPath(AUDIOS_PATH)
        .build()
    private val genresUri = dataSourceBaseUri.buildUpon()
        .appendPath(GENRES_PATH)
        .build()
    private val playlistsUri = dataSourceBaseUri.buildUpon()
        .appendPath(PLAYLISTS_PATH)
        .build()

    /**
     * This flow is used to signal a change in the playlists.
     */
    private val _playlistsChanged = MutableStateFlow(Any())

    override fun isMediaItemCompatible(mediaItemUri: Uri) = mediaItemUri.toString().startsWith(
        dataSourceBaseUri.toString()
    )

    override suspend fun mediaTypeOf(mediaItemUri: Uri) = with(mediaItemUri.toString()) {
        when {
            startsWith(albumsUri.toString()) -> MediaType.ALBUM
            startsWith(artistsUri.toString()) -> MediaType.ARTIST
            startsWith(audiosUri.toString()) -> MediaType.AUDIO
            startsWith(genresUri.toString()) -> MediaType.GENRE
            startsWith(playlistsUri.toString()) -> MediaType.PLAYLIST
            else -> null
        }?.let {
            RequestStatus.Success<_, MediaError>(it)
        } ?: RequestStatus.Error(MediaError.NOT_FOUND)
    }

    override fun status() = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<DataSourceInformation>())
    )

    override fun activity() = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<ActivityTab>())
    )

    override fun albums(sortingRule: SortingRule): Flow<MediaRequestStatus<List<Album>>> = suspend {
        client.getLibraries().toRequestStatus {
            libraries.map { library ->
                client.getLibraryItems(library.id).toResult {
                    results.map {
                        Album(
                            getAlbumUri(it.id),
                            it.media.metadata.title,
                            Uri.EMPTY,
                            it.media.metadata.authorName,
                            it.media.metadata.publishedYear,
                            Thumbnail.Builder()
                                .setUri(Uri.parse(client.getCover(it.id)))
                                .build(),
                        )
                    }
                }.orEmpty()
            }.flatten()
        }
    }.asFlow()

    override fun artists(sortingRule: SortingRule) = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<Artist>())
    )

    override fun genres(sortingRule: SortingRule) = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<Genre>())
    )

    override fun playlists(sortingRule: SortingRule) = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<Playlist>())
    )

    override fun search(query: String) = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<MediaItem<*>>())
    )

    override fun audio(audioUri: Uri) = flowOf(
        RequestStatus.Error<Audio, _>(MediaError.NOT_FOUND)
    )

    override fun album(albumUri: Uri) = flowOf(
        RequestStatus.Error<Pair<Album, List<Audio>>, _>(MediaError.NOT_FOUND)
    )

    override fun artist(artistUri: Uri) = flowOf(
        RequestStatus.Error<Pair<Artist, ArtistWorks>, _>(MediaError.NOT_FOUND)
    )

    override fun genre(genreUri: Uri) = flowOf(
        RequestStatus.Error<Pair<Genre, GenreContent>, _>(MediaError.NOT_FOUND)
    )

    override fun playlist(playlistUri: Uri) = flowOf(
        RequestStatus.Error<Pair<Playlist, List<Audio>>, _>(MediaError.NOT_FOUND)
    )

    override fun audioPlaylistsStatus(audioUri: Uri) = flowOf(
        RequestStatus.Error<List<Pair<Playlist, Boolean>>, _>(MediaError.NOT_FOUND)
    )

    override fun lastPlayedAudio() = flowOf(
        RequestStatus.Error<Audio, _>(MediaError.NOT_FOUND)
    )

    override suspend fun createPlaylist(name: String) =
        RequestStatus.Error<Uri, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun renamePlaylist(playlistUri: Uri, name: String) =
        RequestStatus.Error<Unit, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun deletePlaylist(playlistUri: Uri) =
        RequestStatus.Error<Unit, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun addAudioToPlaylist(
        playlistUri: Uri,
        audioUri: Uri
    ) = RequestStatus.Error<Unit, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun removeAudioFromPlaylist(
        playlistUri: Uri,
        audioUri: Uri
    ) = RequestStatus.Error<Unit, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun onAudioPlayed(audioUri: Uri) =
        RequestStatus.Success<_, MediaError>(Unit)

    private fun getAlbumUri(albumId: String) = albumsUri.buildUpon()
        .appendPath(albumId)
        .build()

    private fun getArtistUri(artistId: String) = artistsUri.buildUpon()
        .appendPath(artistId)
        .build()

    private fun getAudioUri(audioId: String) = audiosUri.buildUpon()
        .appendPath(audioId)
        .build()

    private fun getGenreUri(genre: String) = genresUri.buildUpon()
        .appendPath(genre)
        .build()

    private fun getPlaylistUri(playlistId: String) = playlistsUri.buildUpon()
        .appendPath(playlistId)
        .build()

    private fun onPlaylistsChanged() {
        _playlistsChanged.value = Any()
    }

    companion object {
        private const val ALBUMS_PATH = "albums"
        private const val ARTISTS_PATH = "artists"
        private const val AUDIOS_PATH = "audio"
        private const val GENRES_PATH = "genres"
        private const val PLAYLISTS_PATH = "playlists"

        val ARG_SERVER = ProviderArgument(
            "server",
            String::class,
            R.string.provider_argument_server,
            required = true,
            hidden = false,
            validate = {
                when (it.toHttpUrlOrNull()) {
                    null -> ProviderArgument.ValidationError(
                        "Invalid URL",
                        R.string.provider_argument_validation_error_malformed_http_uri,
                    )

                    else -> null
                }
            }
        )

        val ARG_USERNAME = ProviderArgument(
            "username",
            String::class,
            R.string.provider_argument_username,
            required = true,
            hidden = false,
        )

        val ARG_PASSWORD = ProviderArgument(
            "password",
            String::class,
            R.string.provider_argument_password,
            required = true,
            hidden = true,
            defaultValue = "",
        )
    }
}
