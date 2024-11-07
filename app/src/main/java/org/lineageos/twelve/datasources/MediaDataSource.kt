/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.ArtistWorks
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.GenreContent
import org.lineageos.twelve.models.MediaItem
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.RequestStatus

typealias MediaRequestStatus<T> = RequestStatus<T, MediaError>

/**
 * A data source for media.
 */
interface MediaDataSource {
    /**
     * Check whether this data source can handle the given media item.
     *
     * @param mediaItemUri The media item to check
     * @return Whether this data source can handle the given media item
     */
    fun isMediaItemCompatible(mediaItemUri: Uri): Boolean

    /**
     * Given a compatible media item URI, get its type.
     *
     * @param mediaItemUri The media item to check
     * @return [RequestStatus.Success] if success, [RequestStatus.Error] with an error otherwise
     */
    suspend fun mediaTypeOf(mediaItemUri: Uri): MediaRequestStatus<MediaType>

    /**
     * Get all the albums. All albums must have at least one audio associated with them.
     */
    fun albums(): Flow<MediaRequestStatus<List<Album>>>

    /**
     * Get all the artists. All artists must have at least one audio associated with them.
     */
    fun artists(): Flow<MediaRequestStatus<List<Artist>>>

    /**
     * Get all the genres. All genres must have at least one audio associated with them.
     */
    fun genres(): Flow<MediaRequestStatus<List<Genre>>>

    /**
     * Get all the playlists. A playlist can be empty.
     */
    fun playlists(): Flow<MediaRequestStatus<List<Playlist>>>

    /**
     * Start a search for the given query.
     * Only the following items can be returned: [Album], [Artist], [Audio], [Genre], [Playlist].
     */
    fun search(query: String): Flow<MediaRequestStatus<List<MediaItem<*>>>>

    /**
     * Get the audio information of the given audio.
     */
    fun audio(audioUri: Uri): Flow<MediaRequestStatus<Audio>>

    /**
     * Get the album information and all the tracks of the given album.
     */
    fun album(albumUri: Uri): Flow<MediaRequestStatus<Pair<Album, List<Audio>>>>

    /**
     * Get the artist information and all the works associated with them.
     */
    fun artist(artistUri: Uri): Flow<MediaRequestStatus<Pair<Artist, ArtistWorks>>>

    /**
     * Get the genre information and all the tracks of the given genre.
     */
    fun genre(genreUri: Uri): Flow<MediaRequestStatus<Pair<Genre, GenreContent>>>

    /**
     * Get the playlist information and all the tracks of the given playlist.
     * If the playlist contains an audio that is unavailable, it will be mapped to null.
     */
    fun playlist(playlistUri: Uri): Flow<MediaRequestStatus<Pair<Playlist, List<Audio?>>>>

    /**
     * Get an audio status within all playlists.
     * @param audioUri The URI of the audio
     */
    fun audioPlaylistsStatus(audioUri: Uri): Flow<MediaRequestStatus<List<Pair<Playlist, Boolean>>>>

    /**
     * Create a new playlist. Note that the name shouldn't be considered unique if possible, but
     * this may vary per data source.
     * @param name The name of the playlist
     * @return A [RequestStatus] with the [Uri] of the new playlist if succeeded, an error otherwise
     */
    suspend fun createPlaylist(name: String): MediaRequestStatus<Uri>

    /**
     * Rename a playlist.
     * @param playlistUri The URI of the playlist
     * @param name The new name of the playlist
     * @return [RequestStatus.Success] if success, [RequestStatus.Error] with an error otherwise
     */
    suspend fun renamePlaylist(playlistUri: Uri, name: String): MediaRequestStatus<Unit>

    /**
     * Delete a playlist.
     * @param playlistUri The URI of the playlist
     * @return [RequestStatus.Success] if success, [RequestStatus.Error] with an error otherwise
     */
    suspend fun deletePlaylist(playlistUri: Uri): MediaRequestStatus<Unit>

    /**
     * Add an audio to a playlist.
     * @param playlistUri The URI of the playlist
     * @param audioUri The URI of the audio
     * @return [RequestStatus.Success] if success, [RequestStatus.Error] with an error otherwise
     */
    suspend fun addAudioToPlaylist(playlistUri: Uri, audioUri: Uri): MediaRequestStatus<Unit>

    /**
     * Remove an audio from a playlist.
     * @param playlistUri The URI of the playlist
     * @param audioUri The URI of the audio
     * @return [RequestStatus.Success] if success, [RequestStatus.Error] with an error otherwise
     */
    suspend fun removeAudioFromPlaylist(playlistUri: Uri, audioUri: Uri): MediaRequestStatus<Unit>
}
