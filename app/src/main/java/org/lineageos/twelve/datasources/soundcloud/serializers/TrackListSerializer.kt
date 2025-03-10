/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud.serializers

import org.lineageos.twelve.datasources.soundcloud.models.Track

class TrackListSerializer : LaxedListSerializer<Track>(
    Track.serializer(),
    setOf(
        "id",
        "kind",
        "monetization_model",
        "policy",
    )
)
