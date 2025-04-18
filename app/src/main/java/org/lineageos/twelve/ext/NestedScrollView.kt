/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.view.View
import androidx.core.widget.NestedScrollView

fun NestedScrollView.smoothScrollTo(view: View) = smoothScrollTo(0, view.top)
