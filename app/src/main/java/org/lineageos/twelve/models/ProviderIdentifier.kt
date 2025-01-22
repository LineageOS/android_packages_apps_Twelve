/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import kotlinx.serialization.Serializable

@Serializable
data class ProviderIdentifier(val providerType: ProviderType, val providerTypeId: Long)
