/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import kotlinx.serialization.Serializable

@Serializable
open class ProviderIdentifier(open val type: ProviderType, open val typeId: Long) {
    companion object {
        val DEFAULT = ProviderIdentifier(ProviderType.LOCAL, 0)
    }
}
