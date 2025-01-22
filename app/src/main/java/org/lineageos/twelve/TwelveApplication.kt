/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve

import android.app.Application
import android.content.ComponentName
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.repositories.MediaRepository
import org.lineageos.twelve.repositories.ResumptionPlaylistRepository
import org.lineageos.twelve.services.PlaybackService
import org.lineageos.twelve.ui.coil.ThumbnailMapper

@androidx.annotation.OptIn(UnstableApi::class)
class TwelveApplication : Application(), SingletonImageLoader.Factory {
    private val database by lazy { TwelveDatabase.getInstance(applicationContext) }
    val mediaRepository by lazy { MediaRepository(applicationContext, MainScope(), database) }
    val resumptionPlaylistRepository by lazy { ResumptionPlaylistRepository(database) }

    private val sessionToken by lazy {
        SessionToken(
            applicationContext,
            ComponentName(applicationContext, PlaybackService::class.java)
        )
    }

    private val _mediaControllerFlow = MutableSharedFlow<MediaController>(replay = 1)
    val mediaControllerFlow = _mediaControllerFlow.asSharedFlow()

    override fun onCreate() {
        super.onCreate()

        // Observe dynamic colors changes
        DynamicColors.applyToActivitiesIfAvailable(this)

        MainScope().launch {
            initializeMediaController()
        }
    }

    override fun newImageLoader(context: PlatformContext) = ImageLoader.Builder(this)
        .components {
            add(ThumbnailMapper)
        }
        .build()

    private suspend fun initializeMediaController() {
        val mediaController = MediaController.Builder(this, sessionToken)
            .buildAsync()
            .await()

        _mediaControllerFlow.emit(mediaController)
    }
}
