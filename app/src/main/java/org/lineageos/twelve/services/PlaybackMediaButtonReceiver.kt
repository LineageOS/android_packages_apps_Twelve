/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaButtonReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.lineageos.twelve.TwelveApplication

/**
 * A [MediaButtonReceiver] that only starts [PlaybackService] when there is something to resume.
 */
@OptIn(UnstableApi::class)
class PlaybackMediaButtonReceiver : MediaButtonReceiver() {
    override fun shouldStartForegroundService(context: Context, intent: Intent): Boolean {
        val resumptionPlaylistRepository =
            (context.applicationContext as TwelveApplication).resumptionPlaylistRepository

        val hasResumptionPlaylist = runBlocking(Dispatchers.IO) {
            resumptionPlaylistRepository.getResumptionPlaylist().mediaItemIds.isNotEmpty()
        }

        if (!hasResumptionPlaylist) {
            Log.i(LOG_TAG, "No resumption playlist, not starting the playback service")
        }

        return hasResumptionPlaylist
    }

    companion object {
        private val LOG_TAG = PlaybackMediaButtonReceiver::class.simpleName!!
    }
}
