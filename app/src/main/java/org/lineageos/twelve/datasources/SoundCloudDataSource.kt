/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import okhttp3.Cache
import org.lineageos.twelve.R
import org.lineageos.twelve.datasources.soundcloud.SoundCloudClient
import org.lineageos.twelve.datasources.soundcloud.models.Selection
import org.lineageos.twelve.datasources.soundcloud.models.SystemPlaylist
import org.lineageos.twelve.datasources.soundcloud.models.Track
import org.lineageos.twelve.datasources.soundcloud.models.User
import org.lineageos.twelve.ext.mapAsync
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
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.ProviderArgument
import org.lineageos.twelve.models.ProviderArgument.Companion.getArgument
import org.lineageos.twelve.models.ProviderArgument.Companion.requireArgument
import org.lineageos.twelve.models.Result
import org.lineageos.twelve.models.Result.Companion.getOrNull
import org.lineageos.twelve.models.Result.Companion.map
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.Thumbnail

/**
 * SoundCloud backed data source.
 */
class SoundCloudDataSource(
    arguments: Bundle,
    cache: Cache? = null,
) : MediaDataSource {
    private val clientId = arguments.requireArgument(ARG_CLIENT_ID)
    private val oAuthToken = arguments.getArgument(ARG_OAUTH_TOKEN)

    private val soundCloudClient = SoundCloudClient(clientId, oAuthToken, cache)

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
    private val systemPlaylistsUri = SoundCloudClient.serverUri.buildUpon()
        .appendPath(SYSTEM_PLAYLISTS_PATH)
        .build()

    private val favoritesUri = SoundCloudClient.serverUri.buildUpon()
        .appendPath(FAVORITES_PATH)
        .build()
    private val favoritesPlaylist = Playlist.Builder(favoritesUri)
        .setType(Playlist.Type.FAVORITES)
        .build()

    override fun status() = suspend {
        val me = soundCloudClient.me()

        me.map { user ->
            listOfNotNull(
                DataSourceInformation(
                    "username",
                    LocalizedString.StringResIdLocalizedString(R.string.soundcloud_username),
                    LocalizedString.StringLocalizedString(user.username),
                ),
                DataSourceInformation(
                    "full_name",
                    LocalizedString.StringResIdLocalizedString(R.string.soundcloud_full_name),
                    LocalizedString.StringLocalizedString(user.fullName),
                ),
                user.primaryEmail?.let {
                    DataSourceInformation(
                        "primary_email",
                        LocalizedString.StringResIdLocalizedString(
                            R.string.soundcloud_primary_email
                        ),
                        LocalizedString.StringLocalizedString(it),
                    )
                },
                user.primaryEmailConfirmed?.let {
                    DataSourceInformation(
                        "primary_email_confirmed",
                        LocalizedString.StringResIdLocalizedString(
                            R.string.soundcloud_primary_email_confirmed
                        ),
                        LocalizedString.of(it),
                    )
                },
                user.city?.let {
                    DataSourceInformation(
                        "city",
                        LocalizedString.StringResIdLocalizedString(R.string.soundcloud_city),
                        LocalizedString.StringLocalizedString(it),
                    )
                },
                user.dateOfBirth?.let {
                    DataSourceInformation(
                        "date_of_birth",
                        LocalizedString.StringResIdLocalizedString(
                            R.string.soundcloud_date_of_birth
                        ),
                        LocalizedString.StringResIdLocalizedString(
                            R.string.soundcloud_date_of_birth_format,
                            listOf(it.year, it.month),
                        ),
                    )
                },
                user.downloadsDisabled?.let {
                    DataSourceInformation(
                        "downloads_disabled",
                        LocalizedString.StringResIdLocalizedString(
                            R.string.soundcloud_downloads_disabled
                        ),
                        LocalizedString.of(it),
                    )
                },
            )
        }
    }.asFlow()

    override suspend fun mediaTypeOf(mediaItemUri: Uri) = with(mediaItemUri.toString()) {
        when {
            startsWith(albumsUri.toString()) -> MediaType.ALBUM
            startsWith(artistsUri.toString()) -> MediaType.ARTIST
            startsWith(audiosUri.toString()) -> MediaType.AUDIO
            startsWith(genresUri.toString()) -> MediaType.GENRE
            startsWith(playlistsUri.toString()) -> MediaType.PLAYLIST
            startsWith(systemPlaylistsUri.toString()) -> MediaType.PLAYLIST
            mediaItemUri == favoritesUri -> MediaType.PLAYLIST
            else -> null
        }
    }

    override fun activity() = suspend {
        soundCloudClient.getMixedSelections().map {
            it.collection.map { selection -> selection.toModel() }
        }
    }.asFlow()

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
        Result.Success<_, Error>(listOf(favoritesPlaylist))
    )

    override fun search(query: String) = flowOf(
        Result.Success<_, Error>(listOf<MediaItem<*>>())
    )

    override fun audio(audioUri: Uri) = suspend {
        soundCloudClient.getTrack(
            audioUri.lastPathSegment!!.toInt(),
            audioUri.getQueryParameter("secret_token")
        ).map { it.toModel() }
    }.asFlow()

    override fun album(albumUri: Uri) = suspend {
        val albumId = albumUri.lastPathSegment!!.toInt()

        soundCloudClient.getPlaylist(albumId).map {
            it.toAlbum() to resolveTracks(
                it.tracks.map { track -> track.id }
            ).orEmpty()
        }
    }.asFlow()

    override fun artist(artistUri: Uri) = suspend {
        val artistId = artistUri.lastPathSegment!!.toInt()

        soundCloudClient.getUser(artistId).map {
            it.toModel() to ArtistWorks(
                albums = soundCloudClient.getUserAlbums(
                    artistId
                ).getOrNull()?.collection?.map { set -> set.toAlbum() }.orEmpty(),
                appearsInAlbum = listOf(),
                appearsInPlaylist = listOf(),
            )
        }
    }.asFlow()

    override fun genre(genreUri: Uri) = flowOf(
        Result.Error<Pair<Genre, GenreContent>, _>(Error.NOT_FOUND)
    )

    override fun playlist(playlistUri: Uri) = suspend {
        when {
            playlistUri == favoritesUri -> soundCloudClient.getLikedTracks().map {
                favoritesPlaylist to resolveTracks(
                    it.collection
                ).orEmpty()
            }

            else -> {
                val playlistId = playlistUri.lastPathSegment!!.toInt()

                soundCloudClient.getPlaylist(
                    playlistId,
                    secretToken = playlistUri.getQueryParameter("secret_token"),
                ).map {
                    it.toPlaylist() to resolveTracks(
                        it.tracks.map { track -> track.id }
                    ).orEmpty()
                }
            }
        }
    }.asFlow()

    override fun audioPlaylistsStatus(audioUri: Uri) = flowOf(
        Result.Error<List<Pair<Playlist, Boolean>>, _>(Error.NOT_FOUND)
    )

    override fun lyrics(audioUri: Uri) = flowOf(
        Result.Error<Lyrics, _>(Error.NOT_FOUND)
    )

    override suspend fun createPlaylist(name: String) =
        Result.Error<Uri, _>(Error.NOT_IMPLEMENTED)

    override suspend fun renamePlaylist(playlistUri: Uri, name: String) = when {
        playlistUri == favoritesUri -> Result.Error(Error.IO)
        else -> Result.Error<Unit, _>(Error.NOT_IMPLEMENTED)
    }

    override suspend fun deletePlaylist(playlistUri: Uri) = when {
        playlistUri == favoritesUri -> Result.Error(Error.IO)
        else -> Result.Error<Unit, _>(Error.NOT_IMPLEMENTED)
    }

    override suspend fun addAudioToPlaylist(
        playlistUri: Uri,
        audioUri: Uri
    ) = when {
        playlistUri == favoritesUri -> setFavorite(audioUri, true)
        else -> Result.Error(Error.NOT_IMPLEMENTED)
    }

    override suspend fun removeAudioFromPlaylist(
        playlistUri: Uri,
        audioUri: Uri
    ) = when {
        playlistUri == favoritesUri -> setFavorite(audioUri, false)
        else -> Result.Error(Error.NOT_IMPLEMENTED)
    }

    override suspend fun onAudioPlayed(audioUri: Uri) = Result.Success<_, Error>(Unit)

    override suspend fun setFavorite(
        audioUri: Uri,
        isFavorite: Boolean
    ) = audioUri.lastPathSegment!!.toInt().let {
        when (isFavorite) {
            true -> soundCloudClient.likeTrack(it)
            else -> soundCloudClient.unlikeTrack(it)
        }
    }

    private suspend fun resolveTracks(trackIds: List<Int>) = soundCloudClient.getTracks(
        trackIds
    ).getOrNull()?.sortedBy { track ->
        trackIds.indexOf(track.id)
    }?.mapAsync { track -> track.toModel() }

    private fun Selection.toModel() = ActivityTab(
        id,
        LocalizedString.StringLocalizedString(title),
        items.collection.map {
            when (it) {
                is org.lineageos.twelve.datasources.soundcloud.models.Playlist -> it.toPlaylist()
                is SystemPlaylist -> it.toModel()
                else -> error("Unsupported selection type")
            }
        }
    )

    private fun org.lineageos.twelve.datasources.soundcloud.models.Playlist.toAlbum() = Album.Builder(getAlbumUri(id))
        .setThumbnail(
            Thumbnail.Builder()
                .setUri(artworkUrl)
                .build()
        )
        .setTitle(title)
        .setArtistUri(getArtistUri(user.id))
        .setArtistName(user.fullName)
        .setYear(null) // TODO
        .build()

    private fun org.lineageos.twelve.datasources.soundcloud.models.Playlist.toPlaylist() =
        Playlist.Builder(getPlaylistUri(id))
            .setThumbnail(
                Thumbnail.Builder()
                    .setUri(artworkUrl)
                    .build()
            )
            .setName(title)
            .setType(Playlist.Type.PLAYLIST)
            .build()

    private fun SystemPlaylist.toModel() = Playlist.Builder(getSystemPlaylistUri(id))
        .setThumbnail(
            Thumbnail.Builder()
                .setUri(artworkUrl)
                .build()
        )
        .setName(title)
        .setType(Playlist.Type.PLAYLIST) // TODO
        .build()

    private suspend fun Track.toModel() = Audio.Builder(getAudioUri(id))
        .setThumbnail(
            Thumbnail.Builder()
                .setUri(artworkUrl)
                .build()
        )
        .apply {
            @androidx.annotation.OptIn(UnstableApi::class)
            val transcoding = media.transcodings.firstOrNull {
                !it.snipped
                        && MimeTypes.isAudio(it.format.mimeType)
                        && !it.format.mimeType.startsWith(MimeTypes.AUDIO_MP4)
                        && !it.format.protocol.contains("-encrypted-")
            }

            val directUrl = transcoding?.let {
                soundCloudClient.getTrackTranscodingDirectUrl(it.url).getOrNull()
            }

            setPlaybackUri(directUrl)
            //setMimeType(transcoding?.format?.mimeType) // TODO: Doesn't like audio/mpeg
        }
        .setTitle(title)
        .setType(Audio.Type.MUSIC)
        .setDurationMs(fullDuration)
        .setArtistUri(userId?.let { getArtistUri(it) })
        .setArtistName(publisherMetadata.artist)
        .setAlbumUri(null)
        .setAlbumTitle(null)
        .setDiscNumber(null)
        .setTrackNumber(null)
        .setGenreUri(null)
        .setGenreName(genre)
        .setYear(null)
        .setIsFavorite(false)
        .build()

    private fun User.toModel() = Artist.Builder(getArtistUri(id))
        .setThumbnail(
            Thumbnail.Builder()
                .setUri(avatarUrl)
                .build()
        )
        .setName(fullName)
        .build()

    private fun getAlbumUri(albumId: Int) = albumsUri.buildUpon()
        .appendPath("$albumId")
        .build()

    private fun getArtistUri(artistId: Int) = artistsUri.buildUpon()
        .appendPath("$artistId")
        .build()

    private fun getAudioUri(audioId: Int) = audiosUri.buildUpon()
        .appendPath("$audioId")
        .build()

    private fun getGenreUri(genre: Int) = genresUri.buildUpon()
        .appendPath("$genre")
        .build()

    private fun getPlaylistUri(playlistId: Int) = playlistsUri.buildUpon()
        .appendPath("$playlistId")
        .build()

    private fun getSystemPlaylistUri(systemPlaylistId: String) = systemPlaylistsUri.buildUpon()
        .appendPath(systemPlaylistId)
        .build()

    companion object {
        private const val ALBUMS_PATH = "albums"
        private const val ARTISTS_PATH = "artists"
        private const val AUDIOS_PATH = "audio"
        private const val GENRES_PATH = "genres"
        private const val PLAYLISTS_PATH = "playlists"
        private const val SYSTEM_PLAYLISTS_PATH = "system_playlists"

        private const val FAVORITES_PATH = "favorites"

        val ARG_CLIENT_ID = ProviderArgument(
            "client_id",
            String::class,
            R.string.provider_argument_client_id,
            required = true,
            hidden = true,
        )

        val ARG_OAUTH_TOKEN = ProviderArgument(
            "oauth_token",
            String::class,
            R.string.provider_argument_oauth_token,
            required = false,
            hidden = true,
        )
    }
}
