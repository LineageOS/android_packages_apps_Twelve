/*
 * SPDX-FileCopyrightText: 2025-2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.lineageos.twelve.database.TwelveDatabase
import org.lineageos.twelve.database.entities.SearchHistory
import org.lineageos.twelve.models.Error
import org.lineageos.twelve.models.FlowResult
import org.lineageos.twelve.models.FlowResult.Companion.asFlowResult
import org.lineageos.twelve.models.Result
import org.lineageos.twelve.models.Result.Companion.map
import java.time.Instant

/**
 * Home page view model.
 */
class MainViewModel(application: Application) : TwelveViewModel(application) {
    val navigationProvider = mediaRepository.navigationProvider
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            null,
        )

    private val searchQuery = MutableStateFlow("" to false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults = searchQuery
        .mapLatest {
            val (query, immediate) = it
            if (!immediate && query.isNotEmpty()) {
                delay(500)
            }
            query
        }
        .flatMapLatest { query ->
            query.trim().takeIf { it.isNotEmpty() }?.let {
                mediaRepository.search("%${it}%")
                    .asFlowResult()
                    .onStart { emit(FlowResult.Loading()) }
            } ?: flowOf(FlowResult.Success(listOf()))
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            FlowResult.Loading()
        )

    fun setSearchQuery(query: String, immediate: Boolean = false) {
        searchQuery.value = query to immediate

        if (immediate) {
            addHistoryItem(query)
        }
    }

    suspend fun playAllAudios() = mediaRepository.audios().firstOrNull()?.map { audios ->
        playAudio(audios.shuffled(), 0)
    } ?: Result.Error(Error.INVALID_RESPONSE)

    private val searchHistoryDao = TwelveDatabase.get(application).getSearchHistoryDao()

    val searchHistory = searchHistoryDao.getAll()
        .map { items -> items.map { it.query } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            listOf(),
        )

    fun addHistoryItem(query: String) {
        viewModelScope.launch {
            searchHistoryDao.insert(SearchHistory(query, Instant.now()))
        }
    }

    fun removeSearchQuery(query: String) {
        viewModelScope.launch {
            searchHistoryDao.delete(SearchHistory(query, Instant.now()))
        }
    }
}
