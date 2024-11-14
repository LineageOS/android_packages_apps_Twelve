/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.content.ComponentName
import androidx.annotation.OptIn
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.guava.await
import org.lineageos.twelve.ext.applicationContext
import org.lineageos.twelve.services.PlaybackService
import org.lineageos.twelve.services.PlaybackService.CustomCommand.Companion.sendCustomCommand

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionToken by lazy {
        SessionToken(
            applicationContext,
            ComponentName(applicationContext, PlaybackService::class.java)
        )
    }

    @OptIn(UnstableApi::class)
    suspend fun toggleOffload(offload: Boolean) {
        withMediaController {
            sendCustomCommand(
                PlaybackService.CustomCommand.TOGGLE_OFFLOAD,
                bundleOf(
                    PlaybackService.CustomCommand.ARG_VALUE to offload
                )
            )
        }
    }

    private suspend fun withMediaController(block: suspend MediaController.() -> Unit) {
        val mediaController = MediaController.Builder(applicationContext, sessionToken)
            .buildAsync()
            .await()

        try {
            block(mediaController)
        } finally {
            mediaController.release()
        }
    }

    @OptIn(UnstableApi::class)
    suspend fun toggleSkipSilence(skipSilence: Boolean) {
        val mediaController = MediaController.Builder(applicationContext, sessionToken)
            .buildAsync()
            .await()

        mediaController.sendCustomCommand(
            PlaybackService.buildToggleSkipSilenceCommand(),
            bundleOf(
                PlaybackService.BOOLEAN to skipSilence
            )
        )
        mediaController.release()
    }
}
