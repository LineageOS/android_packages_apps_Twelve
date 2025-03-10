/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.content.res.Resources.Theme
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.Px

@Px
fun Theme.getAttributeDimension(@AttrRes attribute: Int): Float = TypedValue().let {
    resolveAttribute(attribute, it, true)
    it.getDimension(DisplayMetrics().apply { resources.displayMetrics })
}
