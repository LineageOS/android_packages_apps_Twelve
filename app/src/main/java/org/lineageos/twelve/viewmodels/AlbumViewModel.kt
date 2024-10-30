/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import org.lineageos.twelve.models.Audio
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.models.UniqueItem
import kotlin.reflect.safeCast

class AlbumViewModel(application: Application) : TwelveViewModel(application) {
    private val albumUri = MutableStateFlow<Uri?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val album = albumUri
        .filterNotNull()
        .flatMapLatest {
            mediaRepository.album(it)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            RequestStatus.Loading()
        )

    sealed interface AlbumContent : UniqueItem<AlbumContent> {
        data class DiscHeader(val discNumber: Int) : AlbumContent {
            override fun areItemsTheSame(other: AlbumContent) =
                DiscHeader::class.safeCast(other)?.let {
                    discNumber == it.discNumber
                } ?: false

            override fun areContentsTheSame(other: AlbumContent) = true
        }

        class AudioItem(val audio: Audio) : AlbumContent {
            override fun areItemsTheSame(other: AlbumContent) = AudioItem::class.safeCast(
                other
            )?.let {
                audio.areItemsTheSame(it.audio)
            } ?: false

            override fun areContentsTheSame(other: AlbumContent) = AudioItem::class.safeCast(
                other
            )?.let {
                audio.areContentsTheSame(it.audio)
            } ?: false
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val albumContent = album
        .mapLatest {
            when (it) {
                is RequestStatus.Loading -> null

                is RequestStatus.Success -> {
                    val discToTracks = it.data.second.groupBy { audio ->
                        audio.discNumber ?: 1
                    }

                    mutableListOf<AlbumContent>().apply {
                        discToTracks.keys.sorted().forEach { discNumber ->
                            add(AlbumContent.DiscHeader(discNumber))

                            discToTracks[discNumber]?.let { tracks ->
                                addAll(
                                    tracks.map { audio ->
                                        AlbumContent.AudioItem(audio)
                                    }
                                )
                            }
                        }
                    }.toList()
                }

                is RequestStatus.Error -> listOf()
            }
        }
        .filterNotNull()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            listOf()
        )

    fun loadAlbum(albumUri: Uri) {
        this.albumUri.value = albumUri
    }
}
