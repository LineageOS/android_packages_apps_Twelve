/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.net.Uri

fun Uri.contentBaseName() =
    lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')
