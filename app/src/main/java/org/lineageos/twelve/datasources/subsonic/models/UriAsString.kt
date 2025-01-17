/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import android.net.Uri
import kotlinx.serialization.Serializable

@Suppress("Provided_Runtime_Too_Low")
typealias UriAsString = @Serializable(with = UriSerializer::class) Uri
