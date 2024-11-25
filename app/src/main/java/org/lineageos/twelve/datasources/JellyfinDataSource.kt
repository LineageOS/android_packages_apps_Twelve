package org.lineageos.twelve.datasources

import android.net.Uri
import android.os.Bundle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jellyfin.sdk.model.api.BaseItemDto
import org.lineageos.twelve.R
import org.lineageos.twelve.datasources.jellyfin.JellyfinClient
import org.lineageos.twelve.models.ActivityTab
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.ArtistWorks
import org.lineageos.twelve.models.Audio
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

class JellyfinDataSource(arguments: Bundle) : MediaDataSource {
    private val server = arguments.requireArgument(ARG_SERVER)
    private val apiKey = arguments.requireArgument(ARG_API_KEY)

    private val client = JellyfinClient(server, apiKey)

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

    override fun activity(): Flow<MediaRequestStatus<List<ActivityTab>>> {
        TODO("Not yet implemented")
    }

    override fun albums(sortingRule: SortingRule): Flow<MediaRequestStatus<List<Album>>> {
        TODO("Not yet implemented")
    }

    override fun artists(sortingRule: SortingRule) = suspend {
        client.getArtists(sortingRule).toRequestStatus {
            items.map { it.toMediaItemArtist() }
        }
    }.asFlow()

    private fun BaseItemDto.toMediaItemArtist() = Artist(
        uri = getArtistUri(id.toString()),
        name = name ?: "",
        thumbnail = Thumbnail(
            // http://localhost/Artists/{name}/Images/{imageType}/{imageIndex}
            uri = dataSourceBaseUri.buildUpon().apply {
                appendPath(ARTISTS_PATH)
                appendPath(name)
                appendPath("Images")
                appendPath("Primary")
                appendPath("0")
            }.build()
        ),
    )

    override fun genres(sortingRule: SortingRule) = suspend {
        client.getGenres(sortingRule).toRequestStatus {
            items.map { it.toMediaItemGenre() }
        }
    }.asFlow()

    private fun BaseItemDto.toMediaItemGenre() = Genre(
        uri = getGenreUri(id.toString()),
        name = name ?: "",
    )

    override fun playlists(sortingRule: SortingRule): Flow<MediaRequestStatus<List<Playlist>>> {
        TODO("Not yet implemented")
    }

    override fun search(query: String): Flow<MediaRequestStatus<List<MediaItem<*>>>> {
        TODO("Not yet implemented")
    }

    override fun audio(audioUri: Uri): Flow<MediaRequestStatus<Audio>> {
        TODO("Not yet implemented")
    }

    override fun album(albumUri: Uri): Flow<MediaRequestStatus<Pair<Album, List<Audio>>>> {
        TODO("Not yet implemented")
    }

    override fun artist(artistUri: Uri): Flow<MediaRequestStatus<Pair<Artist, ArtistWorks>>> {
        TODO("Not yet implemented")
    }

    override fun genre(genreUri: Uri): Flow<MediaRequestStatus<Pair<Genre, GenreContent>>> {
        TODO("Not yet implemented")
    }

    override fun playlist(playlistUri: Uri): Flow<MediaRequestStatus<Pair<Playlist, List<Audio?>>>> {
        TODO("Not yet implemented")
    }

    override fun audioPlaylistsStatus(audioUri: Uri): Flow<MediaRequestStatus<List<Pair<Playlist, Boolean>>>> {
        TODO("Not yet implemented")
    }

    override suspend fun createPlaylist(name: String): MediaRequestStatus<Uri> {
        TODO("Not yet implemented")
    }

    override suspend fun renamePlaylist(playlistUri: Uri, name: String): MediaRequestStatus<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deletePlaylist(playlistUri: Uri): MediaRequestStatus<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun addAudioToPlaylist(
        playlistUri: Uri, audioUri: Uri
    ): MediaRequestStatus<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun removeAudioFromPlaylist(
        playlistUri: Uri, audioUri: Uri
    ): MediaRequestStatus<Unit> {
        TODO("Not yet implemented")
    }

    private suspend fun <T, O> JellyfinClient.MethodResult<T>.toRequestStatus(
        resultGetter: suspend T.() -> O
    ): RequestStatus<O, MediaError> = when (this) {
        is JellyfinClient.MethodResult.Success -> RequestStatus.Success(result.resultGetter())
        is JellyfinClient.MethodResult.HttpError -> RequestStatus.Error(MediaError.IO)
    }

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
            })

        val ARG_API_KEY = ProviderArgument(
            "api_key",
            String::class,
            R.string.provider_argument_api_key,
            required = true,
            hidden = true
        )
    }
}
