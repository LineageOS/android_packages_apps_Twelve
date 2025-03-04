/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.database.Cursor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.lineageos.twelve.models.ColumnIndexCache

inline fun <T> Flow<Cursor?>.mapEachRow(
    crossinline mapping: suspend (ColumnIndexCache) -> T,
) = map { it.mapEachRow(mapping) }
