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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

object AudioPreloader {
    private val preloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @OptIn(UnstableApi::class)
    fun preload(context: Context, audios: List<MediaItem>) {
        val cache = MediaCache.getCache(context)
        val databaseProvider =
            StandaloneDatabaseProvider(context) // TODO // Note: This should be a singleton in your app.
        val dataSourceFactory = DefaultHttpDataSource.Factory()
        val downloadExecutor = Executors.newFixedThreadPool(2)

        val downloadManager = DownloadManager(
            context,
            databaseProvider,
            cache,
            dataSourceFactory,
            downloadExecutor
        ).apply { setMaxParallelDownloads(1) }

        audios.forEach { audio ->
            preloadScope.launch {
                try {
                    val request = DownloadRequest.Builder(
                        audio.mediaId.toUri().lastPathSegment.toString(),
                        audio.localConfiguration!!.uri
                    )
                        .build()

                    downloadManager.addDownload(request)
                } catch (e: Exception) {
                    Log.e("AudioPreloader", "Failed to queue ${audio.mediaMetadata.title}", e)
                }
            }
        }
        downloadManager.resumeDownloads()

        if (context.packageName.endsWith(".dev")) {
            downloadManager.addListener(object : DownloadManager.Listener {
                override fun onDownloadChanged(
                    manager: DownloadManager,
                    download: Download,
                    finalException: java.lang.Exception?
                ) {
                    if (download.state == Download.STATE_COMPLETED) {
                        Log.i("AudioPreloader", "Cached: ${download.request.id}")
                    } else if (download.state == Download.STATE_FAILED) {
                        Log.e(
                            "AudioPreloader",
                            "Failed to cache: ${download.request.id}",
                            finalException
                        )
                    }
                }
            })
        }
    }
}
