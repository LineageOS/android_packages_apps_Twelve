/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.services

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import org.lineageos.twelve.R
import org.lineageos.twelve.TwelveApplication
import java.util.concurrent.Executors

@OptIn(UnstableApi::class)
class TwelveDownloadService() : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID,
    R.string.download_notification_channel_name,
    0,
) {
    private val downloadNotificationHelper by lazy {
        DownloadNotificationHelper(
            applicationContext,
            DOWNLOAD_NOTIFICATION_CHANNEL_ID,
        )
    }


    override fun getDownloadManager() = DownloadManager(
        applicationContext,
        (applicationContext as TwelveApplication).databaseProvider,
        (applicationContext as TwelveApplication).downloadCache,
        CacheDataSource.Factory()
            .setCache((applicationContext as TwelveApplication).downloadCache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setCacheWriteDataSinkFactory(null)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR),
        Executors.newFixedThreadPool(/* nThreads= */ 6),
    )

    override fun getScheduler() = PlatformScheduler(this, JOB_ID);

    override fun getForegroundNotification(
        downloads: List<Download>, notMetRequirements: Int
    ) = downloadNotificationHelper.buildProgressNotification(
        this,
        R.drawable.ic_music_note,
        null,
        null,
        downloads,
        notMetRequirements,
    )

    companion object {
        const val FOREGROUND_NOTIFICATION_ID = 1001
        const val DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL = 1000L
        const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"
        const val JOB_ID = 1
    }
}
