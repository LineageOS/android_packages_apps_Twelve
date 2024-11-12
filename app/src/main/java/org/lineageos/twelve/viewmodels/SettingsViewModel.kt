/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.content.ComponentName
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.guava.await
import org.lineageos.twelve.ext.applicationContext
import org.lineageos.twelve.ext.setOffloadEnabled
import org.lineageos.twelve.services.PlaybackService

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionToken by lazy {
        SessionToken(
            applicationContext,
            ComponentName(applicationContext, PlaybackService::class.java)
        )
    }

    @OptIn(UnstableApi::class)
    suspend fun toggleOffload(offload: Boolean) {
        val mediaController = MediaController.Builder(applicationContext, sessionToken)
            .buildAsync()
            .await()

        mediaController.setOffloadEnabled(offload)
        mediaController.release()
    }
}
