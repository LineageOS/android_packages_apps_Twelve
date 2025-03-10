/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Publisher metadata of an item.
 *
 * @param id The ID of the item of this metadata
 * @param urn The URN of the item of this metadata
 * @param artist The artist
 * @param albumTitle The album title
 * @param containsMusic Whether the item contains music
 * @param upcOrEan The UPC or EAN of the item
 * @param isrc The ISRC of the item
 * @param explicit Whether the item is explicit
 * @param pLine The publisher data
 * @param pLineForDisplay The publisher data for display
 * @param cLine The catalog data
 * @param cLineForDisplay The catalog data for display
 * @param writerComposer The writer and/or composer
 * @param releaseTitle The release title of the item
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW")
@Serializable
data class PublisherMetadata(
    val id: Int,
    val urn: String,
    val artist: String? = null,
    @SerialName("album_title") val albumTitle: String? = null,
    @SerialName("contains_music") val containsMusic: Boolean? = null,
    @SerialName("upc_or_ean") val upcOrEan: String? = null,
    val isrc: String? = null,
    val explicit: Boolean? = null,
    @SerialName("p_line") val pLine: String? = null,
    @SerialName("p_line_for_display") val pLineForDisplay: String? = null,
    @SerialName("c_line") val cLine: String? = null,
    @SerialName("c_line_for_display") val cLineForDisplay: String? = null,
    @SerialName("writer_composer") val writerComposer: String? = null,
    @SerialName("release_title") val releaseTitle: String? = null,
)
