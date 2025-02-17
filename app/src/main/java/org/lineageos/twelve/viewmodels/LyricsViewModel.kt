/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import org.lineageos.twelve.models.Lyrics

class LyricsViewModel(application: Application) : NowPlayingViewModel(application) {
    fun seekToLine(line: Lyrics.Line) {
        mediaController.value?.apply {
            line.durationMs?.let { durationMs ->
                seekTo(durationMs.first)
            }
        }
    }
}
