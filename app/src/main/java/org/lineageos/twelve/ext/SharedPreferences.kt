/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.content.SharedPreferences
import androidx.core.content.edit
import org.lineageos.twelve.models.RepeatMode
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.SortingStrategy

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
private const val ALBUM_SORTING_STRATEGY_KEY = "sorting_strategy_albums"
private const val ALBUM_SORTING_REVERSE_KEY = "sorting_reverse_albums"
private val ALBUM_SORTING_RULE_DEFAULT = SortingStrategy.CREATION_DATE
private const val ALBUM_SORTING_REVERSE_DEFAULT = true
var SharedPreferences.albumsSortingRule: SortingRule
    get() = SortingRule(
        SortingStrategy.fromName(
            getString(ALBUM_SORTING_STRATEGY_KEY, null) ?: "",
            ALBUM_SORTING_RULE_DEFAULT
        ),
        getBoolean(ALBUM_SORTING_REVERSE_KEY, ALBUM_SORTING_REVERSE_DEFAULT),
    )
    set(value) = edit {
        putString(ALBUM_SORTING_STRATEGY_KEY, value.strategy.name)
        putBoolean(ALBUM_SORTING_REVERSE_KEY, value.reverse)
    }

private const val ARTISTS_SORTING_STRATEGY_KEY = "sorting_strategy_artists"
private const val ARTISTS_SORTING_REVERSE_KEY = "sorting_reverse_artists"
private val ARTISTS_SORTING_RULE_DEFAULT = SortingStrategy.MODIFICATION_DATE
private const val ARTISTS_SORTING_REVERSE_DEFAULT = true
var SharedPreferences.artistsSortingRule: SortingRule
    get() = SortingRule(
        SortingStrategy.fromName(
            getString(ARTISTS_SORTING_STRATEGY_KEY, null) ?: "",
            ARTISTS_SORTING_RULE_DEFAULT
        ),
        getBoolean(ARTISTS_SORTING_REVERSE_KEY, ARTISTS_SORTING_REVERSE_DEFAULT),
    )
    set(value) = edit {
        putString(ARTISTS_SORTING_STRATEGY_KEY, value.strategy.name)
        putBoolean(ARTISTS_SORTING_REVERSE_KEY, value.reverse)
    }

private const val GENRES_SORTING_STRATEGY_KEY = "sorting_strategy_genres"
private const val GENRES_SORTING_REVERSE_KEY = "sorting_reverse_genres"
private val GENRES_SORTING_RULE_DEFAULT = SortingStrategy.NAME
private const val GENRES_SORTING_REVERSE_DEFAULT = false
var SharedPreferences.genresSortingRule: SortingRule
    get() = SortingRule(
        SortingStrategy.fromName(
            getString(GENRES_SORTING_STRATEGY_KEY, null) ?: "",
            GENRES_SORTING_RULE_DEFAULT
        ),
        getBoolean(GENRES_SORTING_REVERSE_KEY, GENRES_SORTING_REVERSE_DEFAULT),
    )
    set(value) = edit {
        putString(GENRES_SORTING_STRATEGY_KEY, value.strategy.name)
        putBoolean(GENRES_SORTING_REVERSE_KEY, value.reverse)
    }

private const val PLAYLISTS_SORTING_STRATEGY_KEY = "sorting_strategy_playlists"
private const val PLAYLISTS_SORTING_REVERSE_KEY = "sorting_reverse_playlists"
private val PLAYLISTS_SORTING_RULE_DEFAULT = SortingStrategy.MODIFICATION_DATE
private const val PLAYLISTS_SORTING_REVERSE_DEFAULT = true
var SharedPreferences.playlistsSortingRule: SortingRule
    get() = SortingRule(
        SortingStrategy.fromName(
            getString(PLAYLISTS_SORTING_STRATEGY_KEY, null) ?: "",
            PLAYLISTS_SORTING_RULE_DEFAULT
        ),
        getBoolean(PLAYLISTS_SORTING_REVERSE_KEY, PLAYLISTS_SORTING_REVERSE_DEFAULT),
    )
    set(value) = edit {
        putString(PLAYLISTS_SORTING_STRATEGY_KEY, value.strategy.name)
        putBoolean(PLAYLISTS_SORTING_REVERSE_KEY, value.reverse)
    }
