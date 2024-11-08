/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.ext.queueFlow

class QueueViewModel(application: Application) : TwelveViewModel(application) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val queue = mediaController
        .filterNotNull()
        .flatMapLatest { it.queueFlow() }
        .flowOn(Dispatchers.Main)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            listOf()
        )

    fun moveItem(from: Int, to: Int) {
        mediaController.value?.moveMediaItem(from, to)
    }

    fun removeItem(index: Int) {
        mediaController.value?.removeMediaItem(index)
    }

    fun playItem(bindingAdapterPosition: Int) {
        mediaController.value?.seekTo(bindingAdapterPosition, 0)
        mediaController.value?.play()
    }
}
