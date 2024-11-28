/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources

import android.content.Context
import android.net.Uri
import android.os.Bundle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.lineageos.twelve.R
import org.lineageos.twelve.datasources.jellyfin.JellyfinClient
import org.lineageos.twelve.datasources.jellyfin.models.Item
import org.lineageos.twelve.datasources.jellyfin.models.ItemType
import org.lineageos.twelve.models.ActivityTab
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.ArtistWorks
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.GenreContent
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.ProviderArgument
import org.lineageos.twelve.models.ProviderArgument.Companion.requireArgument
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.SortingStrategy
import org.lineageos.twelve.models.Thumbnail
import org.lineageos.twelve.utils.toRequestStatus
import org.lineageos.twelve.utils.toResult
import java.util.UUID

/**
 * Jellyfin backed data source.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JellyfinDataSource(
    context: Context,
    arguments: Bundle,
    deviceIdentifier: String,
    cache: Cache? = null,
) : MediaDataSource {
    private val server = arguments.requireArgument(ARG_SERVER)
    private val username = arguments.requireArgument(ARG_USERNAME)
    private val password = arguments.requireArgument(ARG_PASSWORD)

    private val packageName = context.packageName

    private val client = JellyfinClient(
        server, username, password, deviceIdentifier, packageName, cache
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

    override fun activity() = flowOf(RequestStatus.Success<_, MediaError>(listOf<ActivityTab>()))

    override fun albums(sortingRule: SortingRule) = suspend {
        client.getAlbums(sortingRule).toRequestStatus {
            items.map { it.toMediaItemAlbum() }
        }
    }.asFlow()

    override fun artists(sortingRule: SortingRule) = suspend {
        client.getArtists(sortingRule).toRequestStatus {
            items.map { it.toMediaItemArtist() }
        }
    }.asFlow()

    override fun genres(sortingRule: SortingRule) = suspend {
        client.getGenres(sortingRule).toRequestStatus {
            items.map { it.toMediaItemGenre() }
        }
    }.asFlow()

    override fun playlists(sortingRule: SortingRule) = _playlistsChanged.mapLatest {
        client.getPlaylists(sortingRule).toRequestStatus {
            items.map { it.toMediaItemPlaylist() }
        }
    }

    override fun search(query: String) = suspend {
        client.getItems(query).toRequestStatus {
            items.mapNotNull {
                when (it.type) {
                    ItemType.MUSIC_ALBUM -> it.toMediaItemAlbum()

                    ItemType.MUSIC_ARTIST,
                    ItemType.PERSON -> it.toMediaItemArtist()

                    ItemType.AUDIO -> it.toMediaItemAudio()

                    ItemType.GENRE,
                    ItemType.MUSIC_GENRE -> it.toMediaItemGenre()

                    ItemType.PLAYLIST -> it.toMediaItemPlaylist()

                    else -> null
                }
            }
        }
    }.asFlow()

    override fun audio(audioUri: Uri) = suspend {
        val id = UUID.fromString(audioUri.lastPathSegment!!)
        client.getAudio(id).toRequestStatus {
            toMediaItemAudio()
        }
    }.asFlow()

    override fun album(albumUri: Uri) = suspend {
        val id = UUID.fromString(albumUri.lastPathSegment!!)
        client.getAlbum(id).toRequestStatus {
            toMediaItemAlbum() to (client.getAlbumTracks(id).toResult {
                items.map { it.toMediaItemAudio() }
            }.orEmpty())
        }
    }.asFlow()

    override fun artist(artistUri: Uri) = suspend {
        val id = UUID.fromString(artistUri.lastPathSegment!!)
        client.getArtist(id).toRequestStatus {
            toMediaItemArtist() to ArtistWorks(
                albums = client.getArtistWorks(id).toResult {
                    items.map { it.toMediaItemAlbum() }
                }.orEmpty(),
                appearsInAlbum = listOf(),
                appearsInPlaylist = listOf(),
            )
        }
    }.asFlow()

    override fun genre(genreUri: Uri) = suspend {
        val id = UUID.fromString(genreUri.lastPathSegment!!)
        client.getGenre(id).toRequestStatus {
            val items = client.getGenreContent(id).toResult { items }.orEmpty()
            toMediaItemGenre() to GenreContent(
                appearsInAlbums = items.filter { it.type == ItemType.MUSIC_ALBUM }
                    .map { it.toMediaItemAlbum() },
                appearsInPlaylists = items.filter { it.type == ItemType.PLAYLIST }
                    .map { it.toMediaItemPlaylist() },
                audios = items.filter { it.type == ItemType.AUDIO }
                    .map { it.toMediaItemAudio() },
            )
        }
    }.asFlow()

    override fun playlist(playlistUri: Uri) = _playlistsChanged.mapLatest {
        val id = UUID.fromString(playlistUri.lastPathSegment!!)
        client.getPlaylist(id).toRequestStatus {
            toMediaItemPlaylist() to client.getPlaylistTracks(id).toResult {
                items.map { it.toMediaItemAudio() }
            }.orEmpty()
        }
    }

    override fun audioPlaylistsStatus(audioUri: Uri) = _playlistsChanged.mapLatest {
        val audioId = UUID.fromString(audioUri.lastPathSegment!!)
        val sortingRule = SortingRule(SortingStrategy.NAME)

        client.getPlaylists(sortingRule).toRequestStatus {
            items.map { playlist ->
                val playlistItems =
                    client.getPlaylistItemIds(playlist.id).toResult { itemIds }.orEmpty()
                playlist.toMediaItemPlaylist() to (audioId in playlistItems)
            }
        }
    }

    override suspend fun createPlaylist(name: String) = run {
        client.createPlaylist(name).toRequestStatus {
            onPlaylistsChanged()

            getPlaylistUri(id.toString())
        }
    }

    override suspend fun renamePlaylist(playlistUri: Uri, name: String) =
        client.renamePlaylist(UUID.fromString(playlistUri.lastPathSegment!!), name)
            .toRequestStatus {
                onPlaylistsChanged()
            }

    override suspend fun deletePlaylist(playlistUri: Uri) = RequestStatus.Error<Unit, _>(
        MediaError.NOT_IMPLEMENTED
    )

    override suspend fun addAudioToPlaylist(
        playlistUri: Uri, audioUri: Uri
    ) = run {
        val playlistId = UUID.fromString(playlistUri.lastPathSegment!!)
        val audioId = UUID.fromString(audioUri.lastPathSegment!!)
        client.addItemToPlaylist(playlistId, audioId).toRequestStatus {
            onPlaylistsChanged()
        }
    }

    override suspend fun removeAudioFromPlaylist(
        playlistUri: Uri, audioUri: Uri
    ) = run {
        val playlistId = UUID.fromString(playlistUri.lastPathSegment!!)
        val audioId = UUID.fromString(audioUri.lastPathSegment!!)
        client.removeItemFromPlaylist(playlistId, audioId).toRequestStatus {
            onPlaylistsChanged()
        }
    }

    private fun Item.toMediaItemAlbum() = Album(
        uri = getAlbumUri(id.toString()),
        title = name,
        artistUri = getArtistUri(id.toString()),
        artistName = artists?.firstOrNull(),
        year = productionYear,
        thumbnail = Thumbnail(
            uri = Uri.parse(client.getAlbumThumbnail(id)),
        ),
    )

    private fun Item.toMediaItemArtist() = Artist(
        uri = getArtistUri(id.toString()),
        name = name,
        thumbnail = Thumbnail(
            uri = Uri.parse(client.getArtistThumbnail(id)),
        ),
    )

    private fun Item.toMediaItemAudio() = Audio(
        uri = getAudioUri(id.toString()),
        title = name ?: "",
        artistUri = getArtistUri(id.toString()),
        artistName = artists?.firstOrNull(),
        albumUri = getAlbumUri(id.toString()),
        playbackUri = Uri.parse(client.getAudioPlaybackUrl(id)),
        mimeType = container ?: sourceType ?: "",
        type = Audio.Type.MUSIC,
        durationMs = runTimeTicks?.let { it / 10000 } ?: 0,
        albumTitle = album,
        discNumber = parentIndexNumber,
        trackNumber = indexNumber,
        genreUri = getGenreUri(id.toString()),
        genreName = genres?.firstOrNull(),
        year = productionYear,
    )

    private fun Item.toMediaItemGenre() = Genre(
        uri = getGenreUri(id.toString()),
        name = name,
        thumbnail = Thumbnail(
            uri = Uri.parse(client.getGenreThumbnail(id)),
        ),
    )

    private fun Item.toMediaItemPlaylist() = Playlist(
        uri = getPlaylistUri(id.toString()),
        name = name ?: "",
        thumbnail = Thumbnail(
            uri = Uri.parse(client.getPlaylistThumbnail(id)),
        ),
    )

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
