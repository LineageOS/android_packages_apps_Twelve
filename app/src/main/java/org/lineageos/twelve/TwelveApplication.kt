/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve

import android.app.Application
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.MainScope
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.repositories.MediaRepository
import org.lineageos.twelve.repositories.OutputConfigurationRepository
import org.lineageos.twelve.repositories.ProvidersRepository
import org.lineageos.twelve.repositories.ResumptionPlaylistRepository
import org.lineageos.twelve.ui.coil.ThumbnailMapper
import java.io.File
import java.util.concurrent.Executors

@androidx.annotation.OptIn(UnstableApi::class)
class TwelveApplication : Application(), SingletonImageLoader.Factory {
    private val coroutineScope = MainScope()
    private val database by lazy { TwelveDatabase.get(applicationContext) }
    val providersRepository by lazy {
        ProvidersRepository(applicationContext, coroutineScope, database)
    }
    val mediaRepository by lazy {
        MediaRepository(applicationContext, coroutineScope, providersRepository, database)
    }
    val resumptionPlaylistRepository by lazy { ResumptionPlaylistRepository(database) }
    val outputConfigurationRepository by lazy { OutputConfigurationRepository() }

    // Offline cache and download manager
    val databaseProvider by lazy { StandaloneDatabaseProvider(applicationContext) }
    val downloadCache by lazy {
        val downloadDirectory = File(cacheDir, "media_cache")
        SimpleCache(downloadDirectory, NoOpCacheEvictor(), databaseProvider)
    }
    val httpDataSourceFactory by lazy {
        DefaultHttpDataSource.Factory()
    }
    val dataSourceFactory by lazy {
        val upstreamFactory =
            DefaultDataSource.Factory(applicationContext, httpDataSourceFactory)
        CacheDataSource.Factory()
            .setCache(downloadCache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
    }
    val downloadManager by lazy {
        DownloadManager(
            applicationContext,
            databaseProvider,
            downloadCache,
            httpDataSourceFactory,
            Executors.newFixedThreadPool(/* nThreads= */ 6),
        )
    }

    override fun onCreate() {
        super.onCreate()

        // Observe dynamic colors changes
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    override fun newImageLoader(context: PlatformContext) = ImageLoader.Builder(this)
        .components {
            add(ThumbnailMapper)
        }
        .build()
}
