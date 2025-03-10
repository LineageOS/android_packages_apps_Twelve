/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import org.lineageos.twelve.models.ActivityTab
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.ArtistWorks
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.DataSourceInformation
import org.lineageos.twelve.models.Error
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.GenreContent
import org.lineageos.twelve.models.Lyrics
import org.lineageos.twelve.models.MediaItem
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.Result
import org.lineageos.twelve.models.SortingRule

/**
 * SoundCloud backed data source.
 */
class SoundCloudDataSource : MediaDataSource {
    override fun status(): Flow<MediaRequestStatus<List<DataSourceInformation>>> {
        TODO("Not yet implemented")
    }

    override suspend fun mediaTypeOf(mediaItemUri: Uri): MediaType? {
        TODO("Not yet implemented")
    }

    override fun activity(): Flow<MediaRequestStatus<List<ActivityTab>>> {
        TODO("Not yet implemented")
    }

    override fun albums(sortingRule: SortingRule): Flow<MediaRequestStatus<List<Album>>> {
        TODO("Not yet implemented")
    }

    override fun artists(sortingRule: SortingRule): Flow<MediaRequestStatus<List<Artist>>> {
        TODO("Not yet implemented")
    }

    override fun genres(sortingRule: SortingRule): Flow<MediaRequestStatus<List<Genre>>> {
        TODO("Not yet implemented")
    }

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

    override fun playlist(playlistUri: Uri): Flow<MediaRequestStatus<Pair<Playlist, List<Audio>>>> {
        TODO("Not yet implemented")
    }

    override fun audioPlaylistsStatus(audioUri: Uri): Flow<MediaRequestStatus<List<Pair<Playlist, Boolean>>>> {
        TODO("Not yet implemented")
    }

    override fun lyrics(audioUri: Uri): Flow<Result<Lyrics, Error>> {
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
        playlistUri: Uri,
        audioUri: Uri
    ): MediaRequestStatus<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun removeAudioFromPlaylist(
        playlistUri: Uri,
        audioUri: Uri
    ): MediaRequestStatus<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun onAudioPlayed(audioUri: Uri): MediaRequestStatus<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun setFavorite(audioUri: Uri, isFavorite: Boolean): MediaRequestStatus<Unit> {
        TODO("Not yet implemented")
    }
}
