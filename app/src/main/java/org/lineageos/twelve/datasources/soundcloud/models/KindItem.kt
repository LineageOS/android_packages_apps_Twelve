/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud.models

import kotlinx.serialization.Serializable
import org.lineageos.twelve.datasources.soundcloud.serializers.KindItemSerializer

/**
 * [Kind] object item.
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable(with = KindItemSerializer::class)
sealed interface KindItem {
    /**
     * The [Kind] of the resource.
     */
    val kind: Kind
}
