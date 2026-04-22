/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.subsonic.models

import java.time.Instant
import kotlinx.serialization.Serializable

typealias InstantAsString = @Serializable(with = InstantSerializer::class) Instant
