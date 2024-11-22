/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.database.Cursor
import org.lineageos.twelve.models.ColumnIndexCache

fun <T> Cursor?.mapEachRow(
    projection: Array<String>? = null,
    mapping: (ColumnIndexCache) -> T,
) = this?.use { cursor ->
    if (!cursor.moveToFirst()) {
        return@use emptyList<T>()
    }

    val columnIndexCache =
        ColumnIndexCache(cursor, projection?.toList() ?: cursor.columnNames.toList())

    val data = mutableListOf<T>()
    do {
        data.add(mapping(columnIndexCache))
    } while (cursor.moveToNext())

    data.toList()
} ?: emptyList()
