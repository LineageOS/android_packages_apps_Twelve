/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.guava.await
import org.lineageos.twelve.TwelveApplication
import org.lineageos.twelve.ext.applicationContext
import org.lineageos.twelve.ext.shuffleModeEnabled
import org.lineageos.twelve.ext.typedRepeatMode
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.RepeatMode
import org.lineageos.twelve.services.PlaybackService

/**
 * Base view model for all app view models.
 * Here we keep the shared stuff every fragment could use, like access to the repository and
 * the media controller to interact with the playback service.
 */
abstract class TwelveViewModel(application: Application) : AndroidViewModel(application) {
    protected val mediaRepository = getApplication<TwelveApplication>().mediaRepository

    @Suppress("EmptyMethod")
    final override fun <T : Application> getApplication() = super.getApplication<T>()

    protected val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(application)
    }

    private val sessionToken by lazy {
        SessionToken(
            applicationContext,
            ComponentName(applicationContext, PlaybackService::class.java)
        )
    }

    private val mediaControllerFlow = channelFlow {
        val mediaController = MediaController.Builder(applicationContext, sessionToken)
            .buildAsync()
            .await()

        trySend(mediaController)

        awaitClose {
            mediaController.release()
        }
    }

    protected val mediaController = mediaControllerFlow
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    protected var shuffleModeEnabled: Boolean
        get() = mediaController.value?.shuffleModeEnabled ?: false
        set(value) {
            mediaController.value?.apply {
                shuffleModeEnabled = value
                sharedPreferences.shuffleModeEnabled = value
            }
        }

    protected var typedRepeatMode: RepeatMode
        get() = mediaController.value?.typedRepeatMode ?: RepeatMode.NONE
        set(value) {
            mediaController.value?.apply {
                typedRepeatMode = value
                sharedPreferences.typedRepeatMode = value
            }
        }

    fun playAudio(audio: List<Audio>, position: Int) {
        mediaController.value?.apply {
            // Initialize shuffle and repeat modes
            shuffleModeEnabled = sharedPreferences.shuffleModeEnabled
            typedRepeatMode = sharedPreferences.typedRepeatMode

            setMediaItems(audio.map { it.toMedia3MediaItem() }, true)
            prepare()
            seekToDefaultPosition(position)
            play()
        }
    }

    fun addToQueue(audio: List<Audio>) {
        mediaController.value?.apply {
            addMediaItems(audio.map { it.toMedia3MediaItem() })
        }
    }

    fun playNext(audio: List<Audio>) {
        mediaController.value?.apply {
            addMediaItems(1, audio.map { it.toMedia3MediaItem() })
        }
    }
}
