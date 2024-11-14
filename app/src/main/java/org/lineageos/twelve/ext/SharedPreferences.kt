/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.content.SharedPreferences
import androidx.core.content.edit
import org.lineageos.twelve.models.RepeatMode

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
