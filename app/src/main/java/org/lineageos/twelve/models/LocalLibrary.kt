/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.provider.MediaStore
import org.lineageos.twelve.R
import org.lineageos.twelve.query.Query
import org.lineageos.twelve.query.eq

enum class LocalLibrary(val localizedString: LocalizedString) {
    MUSIC(LocalizedString.StringResIdLocalizedString(R.string.library_music)) {
        override val query = MediaStore.Audio.AudioColumns.IS_MUSIC eq "1"
    },
    PODCASTS(LocalizedString.StringResIdLocalizedString(R.string.library_podcast)) {
        override val query = MediaStore.Audio.AudioColumns.IS_PODCAST eq "1"
    },
    AUDIOBOOKS(LocalizedString.StringResIdLocalizedString(R.string.library_audiobook)) {
        override val query = MediaStore.Audio.AudioColumns.IS_AUDIOBOOK eq "1"
    };

    abstract val query: Query
}
