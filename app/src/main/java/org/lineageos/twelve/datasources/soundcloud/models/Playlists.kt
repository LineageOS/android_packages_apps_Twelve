package org.lineageos.twelve.datasources.soundcloud.models

import kotlinx.serialization.Serializable

/**
 * Playlists.
 *
 * @param collection List of [Playlist]
 * @param nextHref ??
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class Playlists(
    val collection: List<Playlist>,
    val nextHref: String,
)
