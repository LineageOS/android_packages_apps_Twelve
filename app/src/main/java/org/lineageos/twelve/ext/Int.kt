/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.twelve.ext

import android.content.res.Resources.getSystem
import kotlin.math.roundToInt

/**
 * dp -> px.
 */
val Int.px
    get() = (this * getSystem().displayMetrics.density).roundToInt()