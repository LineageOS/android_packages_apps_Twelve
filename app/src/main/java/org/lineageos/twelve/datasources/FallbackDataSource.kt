/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.flow.flowOf
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
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.utils.MimeUtils

/**
 * A [MediaDataSource] that lets `content://` and `file://` URIs pass through.
 * File type is assumed to be [MediaType.AUDIO] when `file://` is used.
 *
 * @param contentResolver A [ContentResolver] used to resolve the MIME type.
 */
class FallbackDataSource(
    private val contentResolver: ContentResolver,
) : MediaDataSource {
    override fun status() = flowOf(
        RequestStatus.Success<_, MediaError>(listOf<DataSourceInformation>())
    )

    override fun isMediaItemCompatible(mediaItemUri: Uri) = mediaItemUri.scheme in allowedSchemes

    override suspend fun mediaTypeOf(mediaItemUri: Uri) = when (mediaItemUri.scheme) {
        ContentResolver.SCHEME_CONTENT -> contentResolver.getType(mediaItemUri)?.let(
            MimeUtils::mimeTypeToMediaType
        )

        ContentResolver.SCHEME_FILE -> MediaType.AUDIO
        else -> null
    }?.let {
        RequestStatus.Success<_, MediaError>(it)
    } ?: RequestStatus.Error(MediaError.NOT_FOUND)

    override fun activity() = flowOf(
        RequestStatus.Error<List<ActivityTab>, _>(MediaError.NOT_IMPLEMENTED)
    )

    override fun albums(sortingRule: SortingRule) = flowOf(
        RequestStatus.Error<List<Album>, _>(MediaError.NOT_IMPLEMENTED)
    )

    override fun artists(sortingRule: SortingRule) = flowOf(
        RequestStatus.Error<List<Artist>, _>(MediaError.NOT_IMPLEMENTED)
    )

    override fun genres(sortingRule: SortingRule) = flowOf(
        RequestStatus.Error<List<Genre>, _>(MediaError.NOT_IMPLEMENTED)
    )

    override fun playlists(sortingRule: SortingRule) = flowOf(
        RequestStatus.Error<List<Playlist>, _>(MediaError.NOT_IMPLEMENTED)
    )

    override fun search(query: String) = flowOf(
        RequestStatus.Error<List<MediaItem<*>>, _>(MediaError.NOT_IMPLEMENTED)
    )

    override fun audio(audioUri: Uri) = flowOf(
        RequestStatus.Success<_, MediaError>(
            Audio(
                audioUri,
                playbackUri = audioUri,
                mimeType = when (val scheme = audioUri.scheme) {
                    ContentResolver.SCHEME_CONTENT -> contentResolver.getType(audioUri)
                    ContentResolver.SCHEME_FILE -> null
                    else -> error("Unsupported scheme: $scheme")
                } ?: "audio/*",
                title = "",
                type = Audio.Type.MUSIC,
                durationMs = 0,
                artistUri = Uri.EMPTY,
                artistName = null,
                albumUri = Uri.EMPTY,
                albumTitle = null,
                discNumber = null,
                trackNumber = null,
                genreUri = Uri.EMPTY,
                genreName = null,
                year = null,
            )
        )
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

    override suspend fun createPlaylist(
        name: String
    ) = RequestStatus.Error<Uri, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun renamePlaylist(
        playlistUri: Uri, name: String
    ) = RequestStatus.Error<Unit, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun deletePlaylist(
        playlistUri: Uri
    ) = RequestStatus.Error<Unit, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun addAudioToPlaylist(
        playlistUri: Uri,
        audioUri: Uri
    ) = RequestStatus.Error<Unit, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun removeAudioFromPlaylist(
        playlistUri: Uri,
        audioUri: Uri
    ) = RequestStatus.Error<Unit, _>(MediaError.NOT_IMPLEMENTED)

    override suspend fun onAudioPlayed(audioUri: Uri) = RequestStatus.Success<_, MediaError>(Unit)

    companion object {
        private val allowedSchemes = setOf(
            ContentResolver.SCHEME_CONTENT,
            ContentResolver.SCHEME_FILE,
        )
    }
}
