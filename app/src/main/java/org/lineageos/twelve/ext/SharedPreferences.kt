/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.lineageos.twelve.models.RepeatMode
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.SortingStrategy
import org.lineageos.twelve.repositories.MediaRepository

fun <T> SharedPreferences.preferenceFlow(
    vararg keys: String,
    getter: SharedPreferences.() -> T,
) = callbackFlow {
    val update = {
        trySend(getter())
    }

    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
        if (changedKey in keys) {
            update()
        }
    }

    registerOnSharedPreferenceChangeListener(listener)

    update()

    awaitClose {
        unregisterOnSharedPreferenceChangeListener(listener)
    }
}

// Generic prefs
const val ENABLE_OFFLOAD_KEY = "enable_offload"
private const val ENABLE_OFFLOAD_DEFAULT = true
var SharedPreferences.enableOffload: Boolean
    get() = getBoolean(ENABLE_OFFLOAD_KEY, ENABLE_OFFLOAD_DEFAULT)
    set(value) = edit {
        putBoolean(ENABLE_OFFLOAD_KEY, value)
    }

private const val STOP_PLAYBACK_ON_TASK_REMOVED_KEY = "stop_playback_on_task_removed"
private const val STOP_PLAYBACK_ON_TASK_REMOVED_DEFAULT = true
var SharedPreferences.stopPlaybackOnTaskRemoved: Boolean
    get() = getBoolean(STOP_PLAYBACK_ON_TASK_REMOVED_KEY, STOP_PLAYBACK_ON_TASK_REMOVED_DEFAULT)
    set(value) = edit {
        putBoolean(STOP_PLAYBACK_ON_TASK_REMOVED_KEY, value)
    }

const val SKIP_SILENCE_KEY = "skip_silence"
private const val SKIP_SILENCE_DEFAULT = false
val SharedPreferences.skipSilence: Boolean
    get() = getBoolean(SKIP_SILENCE_KEY, SKIP_SILENCE_DEFAULT)

// Playback prefs
private const val TYPED_REPEAT_MODE_KEY = "typed_repeat_mode"
private val TYPED_REPEAT_MODE_DEFAULT = RepeatMode.NONE.ordinal
var SharedPreferences.typedRepeatMode: RepeatMode
    get() = RepeatMode.entries[getInt(TYPED_REPEAT_MODE_KEY, TYPED_REPEAT_MODE_DEFAULT)]
    set(value) = edit {
        putInt(TYPED_REPEAT_MODE_KEY, value.ordinal)
    }

private const val SHUFFLE_MODE_ENABLED_KEY = "shuffle_mode_enabled"
private const val SHUFFLE_MODE_ENABLED_DEFAULT = false
var SharedPreferences.shuffleModeEnabled: Boolean
    get() = getBoolean(SHUFFLE_MODE_ENABLED_KEY, SHUFFLE_MODE_ENABLED_DEFAULT)
    set(value) = edit {
        putBoolean(SHUFFLE_MODE_ENABLED_KEY, value)
    }

// Sorting prefs
private fun SharedPreferences.getSortingRule(
    strategyKey: String,
    reverseKey: String,
    defaultSortingRule: SortingRule,
) = SortingRule(
    SortingStrategy.entries.firstOrNull {
        it.name == strategyKey
    } ?: defaultSortingRule.strategy,
    getBoolean(reverseKey, defaultSortingRule.reverse),
)

private fun SharedPreferences.setSortingRule(
    strategyKey: String,
    reverseKey: String,
    value: SortingRule
) = edit {
    putString(strategyKey, value.strategy.name)
    putBoolean(reverseKey, value.reverse)
}

const val ALBUMS_SORTING_STRATEGY_KEY = "albums_sorting_strategy"
const val ALBUMS_SORTING_REVERSE_KEY = "albums_sorting_reverse"
var SharedPreferences.albumsSortingRule: SortingRule
    get() = getSortingRule(
        ALBUMS_SORTING_STRATEGY_KEY,
        ALBUMS_SORTING_REVERSE_KEY,
        MediaRepository.defaultAlbumsSortingRule,
    )
    set(value) = setSortingRule(
        ALBUMS_SORTING_STRATEGY_KEY,
        ALBUMS_SORTING_REVERSE_KEY,
        value
    )

const val ARTISTS_SORTING_STRATEGY_KEY = "artists_sorting_strategy"
const val ARTISTS_SORTING_REVERSE_KEY = "artists_sorting_reverse"
var SharedPreferences.artistsSortingRule: SortingRule
    get() = getSortingRule(
        ARTISTS_SORTING_STRATEGY_KEY,
        ARTISTS_SORTING_REVERSE_KEY,
        MediaRepository.defaultArtistsSortingRule,
    )
    set(value) = setSortingRule(
        ARTISTS_SORTING_STRATEGY_KEY,
        ARTISTS_SORTING_REVERSE_KEY,
        value
    )

const val GENRES_SORTING_STRATEGY_KEY = "genres_sorting_strategy"
const val GENRES_SORTING_REVERSE_KEY = "genres_sorting_reverse"
var SharedPreferences.genresSortingRule: SortingRule
    get() = getSortingRule(
        GENRES_SORTING_STRATEGY_KEY,
        GENRES_SORTING_REVERSE_KEY,
        MediaRepository.defaultGenresSortingRule,
    )
    set(value) = setSortingRule(
        GENRES_SORTING_STRATEGY_KEY,
        GENRES_SORTING_REVERSE_KEY,
        value
    )

const val PLAYLISTS_SORTING_STRATEGY_KEY = "playlists_sorting_strategy"
const val PLAYLISTS_SORTING_REVERSE_KEY = "playlists_sorting_reverse"
var SharedPreferences.playlistsSortingRule: SortingRule
    get() = getSortingRule(
        PLAYLISTS_SORTING_STRATEGY_KEY,
        PLAYLISTS_SORTING_REVERSE_KEY,
        MediaRepository.defaultPlaylistsSortingRule,
    )
    set(value) = setSortingRule(
        PLAYLISTS_SORTING_STRATEGY_KEY,
        PLAYLISTS_SORTING_REVERSE_KEY,
        value
    )
