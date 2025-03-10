/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:UseSerializers(UriSerializer::class)

package org.lineageos.twelve.datasources.soundcloud.models

import android.net.Uri
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.lineageos.twelve.datasources.soundcloud.serializers.UriSerializer

/**
 * Error.
 *
 * @param code Error code
 * @param message Error message
 * @param link Error link
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Error(
    val code: Int,
    val message: String,
    val link: Uri,
)
