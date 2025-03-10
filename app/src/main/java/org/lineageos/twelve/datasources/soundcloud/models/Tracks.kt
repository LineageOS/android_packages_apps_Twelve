package org.lineageos.twelve.datasources.soundcloud.models

import kotlinx.serialization.Serializable

/**
 * Tracks.
 *
 * @param collection The list of tracks
 * @param nextHref ??
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Tracks(
    val collection: List<Track>,
    val nextHref: String,
)
