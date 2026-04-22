/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.ampache.models

import java.time.Instant
import kotlinx.serialization.Serializable
import org.lineageos.twelve.datasources.ampache.serializers.Iso8601InstantSerializer

typealias InstantAsIso8061String = @Serializable(with = Iso8601InstantSerializer::class) Instant
