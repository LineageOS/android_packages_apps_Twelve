/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.lineageos.twelve.services.TwelveDownloadService
import java.util.concurrent.Executors

object AudioPreloader {
    private val preloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @OptIn(UnstableApi::class)
    fun preload(context: Context, audios: List<MediaItem>) {
        audios.forEach { audio ->
            preloadScope.launch {
                try {
                    val request = DownloadRequest.Builder(
                        audio.mediaId.toUri().toString(),
                        audio.localConfiguration!!.uri
                    )
                        .build()

                    DownloadService.sendAddDownload(context, TwelveDownloadService::class.java, request, false)
                } catch (e: Exception) {
                    Log.e("AudioPreloader", "Failed to queue ${audio.mediaMetadata.title}", e)
                }
            }
        }
    }
}
