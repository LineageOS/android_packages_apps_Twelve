/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import androidx.media3.common.MediaItem

data class QueueItem(
    val mediaItem: MediaItem,
    val isCurrent: Boolean,
)
