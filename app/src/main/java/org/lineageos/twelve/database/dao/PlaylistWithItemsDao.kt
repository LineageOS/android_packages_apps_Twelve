/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.database.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import org.lineageos.twelve.database.TwelveDatabase

@Dao
@Suppress("FunctionName")
abstract class PlaylistWithItemsDao(database: TwelveDatabase) {
    private val itemDao = database.getItemDao()
    private val playlistDao = database.getPlaylistDao()
    private val playlistItemCrossRefDao = database.getPlaylistItemCrossRefDao()

    /**
     * Add an item to a playlist (creates a cross-reference).
     */
    @Transaction
    open suspend fun addItemToPlaylist(playlistId: Long, itemUri: Uri) {
        _addItemToPlaylist(playlistId, itemDao.getOrInsert(itemUri).id)
    }

    /**
     * Add items to a playlist (creates a cross-reference).
     */
    @Transaction
    open suspend fun addItemsToPlaylist(playlistId: Long, itemUris: List<Uri>) {
        itemUris.forEach { itemUri ->
            _addItemToPlaylist(playlistId, itemDao.getOrInsert(itemUri).id)
        }
    }

    /**
     * Remove an item from a playlist (deletes the cross-reference) and delete the item if it's the
     * last association or if the user never listened to it.
     */
    @Transaction
    open suspend fun removeItemFromPlaylist(playlistId: Long, itemUri: Uri) {
        // The item may not exist, in which case we have to do nothing
        itemDao._getIdByUri(itemUri)?.let { id ->
            _removeItemFromPlaylist(playlistId, id)

            // Check if the item is orphan
            itemDao._deleteIfOrphan(id)
        }
    }

    /**
     * Get a flow of the playlists that includes (or not) the given item.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPlaylistsWithItemStatus(audioUri: Uri) = itemDao._getIdFlowByUri(
        audioUri
    ).flatMapLatest {
        playlistDao._getPlaylistsWithItemStatus(it)
    }

    /**
     * Add an item to a playlist (creates a cross-reference) and updates last modified and track
     * count.
     */
    open suspend fun _addItemToPlaylist(playlistId: Long, itemId: Long) {
        playlistItemCrossRefDao._addItemToPlaylist(playlistId, itemId)
        playlistDao._increaseTrackCount(playlistId)
        playlistDao._updateLastModified(playlistId)
    }

    /**
     * Remove an item from a playlist (deletes the cross-reference) and updates last modified and
     * track count.
     */
    open suspend fun _removeItemFromPlaylist(playlistId: Long, itemId: Long) {
        playlistItemCrossRefDao._removeItemFromPlaylist(playlistId, itemId)
        playlistDao._decreaseTrackCount(playlistId)
        playlistDao._updateLastModified(playlistId)
    }
}
