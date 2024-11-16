/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.graphics.Bitmap
import android.net.Uri
import androidx.media3.common.MediaMetadata

/**
 * A thumbnail for a media item. It can be a URI or a bitmap. Both can be defined, in that case the
 * URI should take precedence. At least one of the two should be non-null.
 *
 * @param uri The URI of the thumbnail.
 * @param type the type of the thumbnail.
 */
data class Thumbnail(
    val uri: Uri,
    val type: Type = Type.OTHER,
) : Comparable<Thumbnail> {
    /**
     * ID3-like picture types.
     */
    enum class Type(val media3Value: @MediaMetadata.PictureType Int) {
        /**
         * Other.
         */
        OTHER(MediaMetadata.PICTURE_TYPE_OTHER),

        /**
         * 32x32 pixels 'file icon' (PNG only).
         */
        FILE_ICON(MediaMetadata.PICTURE_TYPE_FILE_ICON),

        /**
         * Other file icon.
         */
        FILE_ICON_OTHER(MediaMetadata.PICTURE_TYPE_FILE_ICON_OTHER),

        /**
         * Cover (front).
         */
        FRONT_COVER(MediaMetadata.PICTURE_TYPE_FRONT_COVER),

        /**
         * Cover (back).
         */
        BACK_COVER(MediaMetadata.PICTURE_TYPE_BACK_COVER),

        /**
         * Leaflet page.
         */
        LEAFLET_PAGE(MediaMetadata.PICTURE_TYPE_LEAFLET_PAGE),

        /**
         * Media (e.g. label side of CD).
         */
        MEDIA(MediaMetadata.PICTURE_TYPE_MEDIA),

        /**
         * Lead artist/lead performer/soloist.
         */
        LEAD_ARTIST_PERFORMER(MediaMetadata.PICTURE_TYPE_LEAD_ARTIST_PERFORMER),

        /**
         * Artist/performer.
         */
        ARTIST_PERFORMER(MediaMetadata.PICTURE_TYPE_ARTIST_PERFORMER),

        /**
         * Conductor.
         */
        CONDUCTOR(MediaMetadata.PICTURE_TYPE_CONDUCTOR),

        /**
         * Band/Orchestra.
         */
        BAND_ORCHESTRA(MediaMetadata.PICTURE_TYPE_BAND_ORCHESTRA),

        /**
         * Composer.
         */
        COMPOSER(MediaMetadata.PICTURE_TYPE_COMPOSER),

        /**
         * Lyricist/text writer.
         */
        LYRICIST(MediaMetadata.PICTURE_TYPE_LYRICIST),

        /**
         * Recording Location.
         */
        RECORDING_LOCATION(MediaMetadata.PICTURE_TYPE_RECORDING_LOCATION),

        /**
         * During recording.
         */
        DURING_RECORDING(MediaMetadata.PICTURE_TYPE_DURING_RECORDING),

        /**
         * During performance.
         */
        DURING_PERFORMANCE(MediaMetadata.PICTURE_TYPE_DURING_PERFORMANCE),

        /**
         * Movie/video screen capture.
         */
        MOVIE_VIDEO_SCREEN_CAPTURE(MediaMetadata.PICTURE_TYPE_MOVIE_VIDEO_SCREEN_CAPTURE),

        /**
         * A bright coloured fish.
         * [what?](https://musicfans.stackexchange.com/questions/14446/reason-for-id3-bright-colored-fish)
         */
        A_BRIGHT_COLORED_FISH(MediaMetadata.PICTURE_TYPE_A_BRIGHT_COLORED_FISH),

        /**
         * Illustration.
         */
        ILLUSTRATION(MediaMetadata.PICTURE_TYPE_ILLUSTRATION),

        /**
         * Band/artist logotype.
         */
        BAND_ARTIST_LOGO(MediaMetadata.PICTURE_TYPE_BAND_ARTIST_LOGO),

        /**
         * Publisher/Studio logotype.
         */
        PUBLISHER_STUDIO_LOGO(MediaMetadata.PICTURE_TYPE_PUBLISHER_STUDIO_LOGO);

        companion object {
            fun fromMedia3Value(value: @MediaMetadata.PictureType Int) = entries.firstOrNull {
                it.media3Value == value
            } ?: throw Exception("Unknown picture type $value")
        }
    }

    override fun compareTo(other: Thumbnail) = compareValuesBy(
        this, other,
        Thumbnail::uri,
        Thumbnail::type,
    )
}
