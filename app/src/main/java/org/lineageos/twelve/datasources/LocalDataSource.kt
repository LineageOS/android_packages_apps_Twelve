/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.core.database.getStringOrNull
import androidx.core.os.bundleOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.database.entities.Item
import org.lineageos.twelve.ext.mapEachRow
import org.lineageos.twelve.ext.queryFlow
import org.lineageos.twelve.models.Album
import org.lineageos.twelve.models.Artist
import org.lineageos.twelve.models.ArtistWorks
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.Genre
import org.lineageos.twelve.models.GenreContent
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.models.Playlist
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.Thumbnail
import org.lineageos.twelve.query.Query
import org.lineageos.twelve.query.and
import org.lineageos.twelve.query.eq
import org.lineageos.twelve.query.`in`
import org.lineageos.twelve.query.`is`
import org.lineageos.twelve.query.like
import org.lineageos.twelve.query.neq
import org.lineageos.twelve.query.query

/**
 * [MediaStore.Audio] backed data source.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocalDataSource(context: Context, private val database: TwelveDatabase) : MediaDataSource {
    private val contentResolver = context.contentResolver

    private val albumsUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    private val artistsUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
    private val genresUri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
    private val audiosUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    private val mapAlbum = { it: Cursor, indexCache: Array<Int> ->
        var i = 0

        val albumId = it.getLong(indexCache[i++])
        val album = it.getString(indexCache[i++])
        val artistId = it.getLong(indexCache[i++])
        val artist = it.getString(indexCache[i++])
        val lastYear = it.getInt(indexCache[i++])

        val uri = ContentUris.withAppendedId(albumsUri, albumId)
        val artistUri = ContentUris.withAppendedId(artistsUri, artistId)

        val thumbnail = runCatching {
            contentResolver.loadThumbnail(
                uri, Size(512, 512), null
            )
        }.getOrNull()?.let {
            Thumbnail(bitmap = it, type = Thumbnail.Type.FRONT_COVER)
        }

        Album(
            uri,
            album,
            artistUri,
            artist,
            lastYear.takeIf { it != 0 },
            thumbnail,
        )
    }

    private val mapArtist = { it: Cursor, indexCache: Array<Int> ->
        var i = 0

        val artistId = it.getLong(indexCache[i++])
        val artist = it.getString(indexCache[i++])

        val uri = ContentUris.withAppendedId(artistsUri, artistId)

        val thumbnail = runCatching {
            contentResolver.loadThumbnail(
                uri, Size(512, 512), null
            )
        }.getOrNull()?.let {
            Thumbnail(bitmap = it, type = Thumbnail.Type.BAND_ARTIST_LOGO)
        }

        Artist(
            uri,
            artist,
            thumbnail,
        )
    }

    private val mapGenre = { it: Cursor, indexCache: Array<Int> ->
        var i = 0

        val genreId = it.getLong(indexCache[i++])
        val name = it.getStringOrNull(indexCache[i++])

        val uri = ContentUris.withAppendedId(genresUri, genreId)

        Genre(
            uri,
            name,
        )
    }

    private val mapAudio = { it: Cursor, indexCache: Array<Int> ->
        var i = 0

        val audioId = it.getLong(indexCache[i++])
        val mimeType = it.getString(indexCache[i++])
        val title = it.getString(indexCache[i++])
        val isMusic = it.getInt(indexCache[i++]) != 0
        val isPodcast = it.getInt(indexCache[i++]) != 0
        val isAudiobook = it.getInt(indexCache[i++]) != 0
        val duration = it.getInt(indexCache[i++])
        val artistId = it.getLong(indexCache[i++])
        val artist = it.getString(indexCache[i++])
        val albumId = it.getLong(indexCache[i++])
        val album = it.getString(indexCache[i++])
        val track = it.getInt(indexCache[i++])
        val genreId = it.getLong(indexCache[i++])
        val genre = it.getStringOrNull(indexCache[i++])
        val year = it.getInt(indexCache[i++])

        val isRecording = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            it.getInt(indexCache[i++]) != 0
        } else {
            false
        }

        val uri = ContentUris.withAppendedId(audiosUri, audioId)
        val artistUri = ContentUris.withAppendedId(artistsUri, artistId)
        val albumUri = ContentUris.withAppendedId(albumsUri, albumId)
        val genreUri = ContentUris.withAppendedId(genresUri, genreId)

        val audioType = when {
            isMusic -> Audio.Type.MUSIC
            isPodcast -> Audio.Type.PODCAST
            isAudiobook -> Audio.Type.AUDIOBOOK
            isRecording -> Audio.Type.RECORDING
            else -> Audio.Type.MUSIC
        }

        val (discNumber, discTrack) = track.takeUnless { it == 0 }?.let {
            when (track > 1000) {
                true -> track / 1000 to track % 1000
                false -> null to track
            }
        } ?: (null to null)

        Audio(
            uri,
            uri,
            mimeType,
            title,
            audioType,
            duration,
            artistUri,
            artist,
            albumUri,
            album,
            discNumber,
            discTrack,
            genreUri,
            genre,
            year.takeIf { it != 0 },
        )
    }

    override fun isMediaItemCompatible(mediaItemUri: Uri) = listOf(
        albumsUri,
        artistsUri,
        genresUri,
        audiosUri,
        playlistsBaseUri,
    ).any {
        mediaItemUri.toString().startsWith(it.toString())
    }

    override suspend fun mediaTypeOf(mediaItemUri: Uri) = with(mediaItemUri.toString()) {
        when {
            startsWith(albumsUri.toString()) -> MediaType.ALBUM
            startsWith(artistsUri.toString()) -> MediaType.ARTIST
            startsWith(genresUri.toString()) -> MediaType.GENRE
            startsWith(audiosUri.toString()) -> MediaType.AUDIO
            startsWith(playlistsBaseUri.toString()) -> MediaType.PLAYLIST
            else -> null
        }?.let {
            RequestStatus.Success<_, MediaError>(it)
        } ?: RequestStatus.Error(MediaError.NOT_FOUND)
    }

    override fun albums() = contentResolver.queryFlow(
        albumsUri,
        albumsProjection,
    ).mapEachRow(albumsProjection, mapAlbum).map {
        RequestStatus.Success<_, MediaError>(it)
    }

    override fun artists() = contentResolver.queryFlow(
        artistsUri,
        artistsProjection,
    ).mapEachRow(artistsProjection, mapArtist).map {
        RequestStatus.Success<_, MediaError>(it)
    }

    override fun genres() = contentResolver.queryFlow(
        genresUri,
        genresProjection,
    ).mapEachRow(genresProjection, mapGenre).map {
        RequestStatus.Success<_, MediaError>(it)
    }

    override fun playlists() = database.getPlaylistDao().getAll()
        .mapLatest { playlists ->
            RequestStatus.Success<_, MediaError>(playlists.map { it.toModel() })
        }

    override fun search(query: String) = combine(
        contentResolver.queryFlow(
            albumsUri,
            albumsProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.AlbumColumns.ALBUM like Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(query),
            )
        ).mapEachRow(albumsProjection, mapAlbum),
        contentResolver.queryFlow(
            artistsUri,
            artistsProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.ArtistColumns.ARTIST like Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(query),
            )
        ).mapEachRow(artistsProjection, mapArtist),
        contentResolver.queryFlow(
            audiosUri,
            audiosProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.AudioColumns.TITLE like Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(query),
            )
        ).mapEachRow(audiosProjection, mapAudio),
        contentResolver.queryFlow(
            genresUri,
            genresProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.GenresColumns.NAME like Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(query),
            )
        ).mapEachRow(genresProjection, mapGenre),
    ) { albums, artists, audios, genres ->
        albums + artists + audios + genres
    }.map { RequestStatus.Success<_, MediaError>(it) }

    override fun audio(audioUri: Uri) = contentResolver.queryFlow(
        audiosUri,
        audiosProjection,
        bundleOf(
            ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                MediaStore.Audio.AudioColumns._ID eq Query.ARG
            },
            ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                ContentUris.parseId(audioUri).toString(),
            ),
        )
    ).mapEachRow(audiosProjection, mapAudio).mapLatest { audios ->
        audios.firstOrNull()?.let {
            RequestStatus.Success<_, MediaError>(it)
        } ?: RequestStatus.Error(MediaError.NOT_FOUND)
    }

    override fun album(albumUri: Uri) = combine(
        contentResolver.queryFlow(
            albumsUri,
            albumsProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.AudioColumns._ID eq Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                    ContentUris.parseId(albumUri).toString(),
                ),
            )
        ).mapEachRow(albumsProjection, mapAlbum),
        contentResolver.queryFlow(
            audiosUri,
            audiosProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.AudioColumns.ALBUM_ID eq Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                    ContentUris.parseId(albumUri).toString(),
                ),
                ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(
                    MediaStore.Audio.AudioColumns.TRACK,
                )
            )
        ).mapEachRow(audiosProjection, mapAudio)
    ) { albums, audios ->
        albums.firstOrNull()?.let { album ->
            RequestStatus.Success<_, MediaError>(album to audios)
        } ?: RequestStatus.Error(MediaError.NOT_FOUND)
    }

    override fun artist(artistUri: Uri) = combine(
        contentResolver.queryFlow(
            artistsUri,
            artistsProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.AudioColumns._ID eq Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                    ContentUris.parseId(artistUri).toString(),
                ),
            )
        ).mapEachRow(artistsProjection, mapArtist),
        contentResolver.queryFlow(
            albumsUri,
            albumsProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.AlbumColumns.ARTIST_ID eq Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                    ContentUris.parseId(artistUri).toString(),
                ),
            )
        ).mapEachRow(albumsProjection, mapAlbum),
        contentResolver.queryFlow(
            audiosUri,
            audioAlbumIdsProjection,
            bundleOf(
                ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                    MediaStore.Audio.AudioColumns.ARTIST_ID eq Query.ARG
                },
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                    ContentUris.parseId(artistUri).toString(),
                ),
                ContentResolver.QUERY_ARG_SQL_GROUP_BY to MediaStore.Audio.AudioColumns.ALBUM_ID,
            )
        ).mapEachRow(audioAlbumIdsProjection) { it, indexCache ->
            // albumId
            it.getLong(indexCache[0])
        }.flatMapLatest { albumIds ->
            contentResolver.queryFlow(
                albumsUri,
                albumsProjection,
                bundleOf(
                    ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                        (MediaStore.Audio.AudioColumns.ARTIST_ID neq Query.ARG) and
                                (MediaStore.Audio.AudioColumns._ID `in` List(albumIds.size) {
                                    Query.ARG
                                })
                    },
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                        ContentUris.parseId(artistUri).toString(),
                        *albumIds
                            .map { it.toString() }
                            .toTypedArray(),
                    ),
                )
            ).mapEachRow(albumsProjection, mapAlbum)
        }
    ) { artists, albums, appearsInAlbum ->
        artists.firstOrNull()?.let { artist ->
            val artistWorks = ArtistWorks(
                albums,
                appearsInAlbum,
                listOf(),
            )

            RequestStatus.Success<_, MediaError>(artist to artistWorks)
        } ?: RequestStatus.Error(MediaError.NOT_FOUND)
    }

    override fun genre(genreUri: Uri) = ContentUris.parseId(genreUri).let { genreId ->
        val (genreSelection, genreSelectionArgs) = when (genreId) {
            0L -> (MediaStore.Audio.AudioColumns.GENRE_ID `is` Query.NULL) to arrayOf()

            else -> (MediaStore.Audio.AudioColumns.GENRE_ID eq Query.ARG) to
                    arrayOf(genreId.toString())
        }

        combine(
            contentResolver.queryFlow(
                genresUri,
                genresProjection,
                bundleOf(
                    ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                        when (genreId) {
                            0L -> MediaStore.Audio.AudioColumns._ID `is` Query.NULL
                            else -> MediaStore.Audio.AudioColumns._ID eq Query.ARG
                        }
                    },
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                        *when (genreId) {
                            0L -> arrayOf()
                            else -> arrayOf(genreId.toString())
                        }
                    ),
                )
            ).mapEachRow(genresProjection, mapGenre),
            contentResolver.queryFlow(
                audiosUri,
                audioAlbumIdsProjection,
                bundleOf(
                    ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                        genreSelection
                    },
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                        *genreSelectionArgs,
                    ),
                    ContentResolver.QUERY_ARG_SQL_GROUP_BY to
                            MediaStore.Audio.AudioColumns.ALBUM_ID,
                )
            ).mapEachRow(audioAlbumIdsProjection) { it, indexCache ->
                // albumId
                it.getLong(indexCache[0])
            }.flatMapLatest { albumIds ->
                contentResolver.queryFlow(
                    albumsUri,
                    albumsProjection,
                    bundleOf(
                        ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                            MediaStore.Audio.AudioColumns._ID `in` List(albumIds.size) {
                                Query.ARG
                            }
                        },
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                            *albumIds
                                .map { it.toString() }
                                .toTypedArray(),
                        ),
                    )
                ).mapEachRow(albumsProjection, mapAlbum)
            },
            contentResolver.queryFlow(
                audiosUri,
                audiosProjection,
                bundleOf(
                    ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                        genreSelection
                    },
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to arrayOf(
                        *genreSelectionArgs,
                    ),
                )
            ).mapEachRow(audiosProjection, mapAudio)
        ) { genres, appearsInAlbums, audios ->
            val genre = genres.firstOrNull() ?: when (genreId) {
                0L -> Genre(genreUri, null)
                else -> null
            }

            genre?.let {
                val genreContent = GenreContent(
                    appearsInAlbums,
                    listOf(),
                    audios,
                )

                RequestStatus.Success<_, MediaError>(it to genreContent)
            } ?: RequestStatus.Error(MediaError.NOT_FOUND)
        }
    }

    override fun playlist(playlistUri: Uri) = database.getPlaylistDao().getPlaylistWithItems(
        ContentUris.parseId(playlistUri)
    ).flatMapLatest { data ->
        data?.let { playlistWithItems ->
            val playlist = playlistWithItems.playlist.toModel()

            audios(playlistWithItems.items.map(Item::audioUri))
                .mapLatest { items ->
                    RequestStatus.Success<_, MediaError>(playlist to items)
                }
        } ?: flowOf(
            RequestStatus.Error(
                MediaError.NOT_FOUND
            )
        )
    }

    override fun audioPlaylistsStatus(audioUri: Uri) =
        database.getPlaylistWithItemsDao().getPlaylistsWithItemStatus(
            audioUri
        ).mapLatest { data ->
            RequestStatus.Success<_, MediaError>(
                data.map {
                    it.playlist.toModel() to it.value
                }
            )
        }

    override suspend fun createPlaylist(name: String) = database.getPlaylistDao().create(
        name
    ).let {
        RequestStatus.Success<_, MediaError>(ContentUris.withAppendedId(playlistsBaseUri, it))
    }

    override suspend fun renamePlaylist(playlistUri: Uri, name: String) =
        database.getPlaylistDao().rename(
            ContentUris.parseId(playlistUri), name
        ).let {
            RequestStatus.Success<_, MediaError>(Unit)
        }

    override suspend fun deletePlaylist(playlistUri: Uri) = database.getPlaylistDao().delete(
        ContentUris.parseId(playlistUri)
    ).let {
        RequestStatus.Success<_, MediaError>(Unit)
    }

    override suspend fun addAudioToPlaylist(
        playlistUri: Uri,
        audioUri: Uri,
    ) = database.getPlaylistWithItemsDao().addItemToPlaylist(
        ContentUris.parseId(playlistUri),
        audioUri
    ).let {
        RequestStatus.Success<_, MediaError>(Unit)
    }

    override suspend fun removeAudioFromPlaylist(
        playlistUri: Uri,
        audioUri: Uri,
    ) = database.getPlaylistWithItemsDao().removeItemFromPlaylist(
        ContentUris.parseId(playlistUri),
        audioUri
    ).let {
        RequestStatus.Success<_, MediaError>(Unit)
    }

    /**
     * Given a list of audio URIs, return a list of [Audio], where null if the audio hasn't been
     * found.
     */
    private fun audios(audioUris: List<Uri>) = contentResolver.queryFlow(
        audiosUri,
        audiosProjection,
        bundleOf(
            ContentResolver.QUERY_ARG_SQL_SELECTION to query {
                MediaStore.Audio.AudioColumns._ID `in` List(audioUris.size) {
                    Query.ARG
                }
            },
            ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS to audioUris.map {
                ContentUris.parseId(it).toString()
            }.toTypedArray(),
        )
    )
        .mapEachRow(audiosProjection, mapAudio)
        .mapLatest { audios ->
            audioUris.map { audioUri ->
                audios.firstOrNull { it.uri == audioUri }
            }
        }

    companion object {
        private val albumsProjection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AlbumColumns.ALBUM,
            MediaStore.Audio.AlbumColumns.ARTIST_ID,
            MediaStore.Audio.AlbumColumns.ARTIST,
            MediaStore.Audio.AlbumColumns.LAST_YEAR,
        )

        private val artistsProjection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.ArtistColumns.ARTIST,
        )

        private val genresProjection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.GenresColumns.NAME,
        )

        private val audiosProjection = mutableListOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.MIME_TYPE,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.IS_MUSIC,
            MediaStore.Audio.AudioColumns.IS_PODCAST,
            MediaStore.Audio.AudioColumns.IS_AUDIOBOOK,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.ARTIST_ID,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.AudioColumns.TRACK,
            MediaStore.Audio.AudioColumns.GENRE_ID,
            MediaStore.Audio.AudioColumns.GENRE,
            MediaStore.Audio.AudioColumns.YEAR,
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(MediaStore.Audio.AudioColumns.IS_RECORDING)
            }
        }.toTypedArray()

        private val audioAlbumIdsProjection = arrayOf(
            MediaStore.Audio.AudioColumns.ALBUM_ID,
        )

        /**
         * Dummy internal database scheme.
         */
        private const val DATABASE_SCHEME = "twelve_database"

        /**
         * Dummy database playlists authority.
         */
        private const val PLAYLISTS_AUTHORITY = "playlists"

        /**
         * Dummy internal database playlists [Uri].
         */
        private val playlistsBaseUri = Uri.Builder()
            .scheme(DATABASE_SCHEME)
            .authority(PLAYLISTS_AUTHORITY)
            .build()

        private fun org.lineageos.twelve.database.entities.Playlist.toModel() = Playlist(
            ContentUris.withAppendedId(playlistsBaseUri, id),
            name,
        )
    }
}
