/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.content.Context
import androidx.annotation.StringRes

data class LocalizedString(
    val value: String,
    @StringRes val stringResId: Int? = null,
    val stringResIdArgs: List<Any>? = null,
) {
    override fun toString() = value

    fun getString(context: Context) = stringResId?.let { stringResId ->
        stringResIdArgs?.let { stringResIdArgs ->
            context.getString(stringResId, *stringResIdArgs.toTypedArray())
        } ?: context.getString(stringResId)
    } ?: value
}
