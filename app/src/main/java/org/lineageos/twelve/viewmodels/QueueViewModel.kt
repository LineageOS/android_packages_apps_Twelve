/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import org.lineageos.twelve.ext.queueFlow

class QueueViewModel(application: Application) : TwelveViewModel(application) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val queue = mediaControllerFlow
        .flatMapLatest { it.queueFlow() }
        .flowOn(Dispatchers.Main)

    fun moveItem(from: Int, to: Int) {
        mediaController.value?.moveMediaItem(from, to)
    }

    fun removeItem(index: Int) {
        mediaController.value?.removeMediaItem(index)
    }

    fun playItem(index: Int) {
        mediaController.value?.apply {
            seekTo(index, 0)
            play()
        }
    }
}
