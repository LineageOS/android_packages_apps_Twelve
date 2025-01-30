/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import org.lineageos.twelve.R
import org.lineageos.twelve.datasources.JellyfinDataSource
import org.lineageos.twelve.datasources.MediaDataSource
import org.lineageos.twelve.datasources.SubsonicDataSource

/**
 * Data provider type. This regulates how data should be fetched, usually having a [MediaDataSource]
 * for each one.
 *
 * @param nameStringResId String resource ID of the display name of the provider
 * @param iconDrawableResId The drawable resource ID of the provider
 */
@Parcelize
enum class ProviderType(
    @StringRes val nameStringResId: Int,
    @DrawableRes val iconDrawableResId: Int,
) : Parcelable {
    /**
     * Local provider.
     */
    LOCAL(
        R.string.provider_type_local,
        R.drawable.ic_smartphone,
    ),

    /**
     * Subsonic / OpenSubsonic / Navidrome provider.
     *
     * @see <a href="https://www.subsonic.org/pages/index.jsp">Subsonic home page</a>
     * @see <a href="https://opensubsonic.netlify.app">OpenSubsonic home page</a>
     * @see <a href="https://navidrome.org">Navidrome home page</a>
     */
    SUBSONIC(
        R.string.provider_type_subsonic,
        R.drawable.ic_sailing,
    ),

    /**
     * Jellyfin provider.
     *
     * [Home page](https://jellyfin.org)
     */
    JELLYFIN(
        R.string.provider_type_jellyfin,
        R.drawable.ic_jellyfin,
    );

    companion object {
        /**
         * Get the arguments required to start a session for a given provider type.
         *
         * @param type The provider type
         * @return The list of arguments required to start a session
         */
        fun getArguments(type: ProviderType) = when (type) {
            LOCAL -> listOf()

            SUBSONIC -> listOf(
                SubsonicDataSource.ARG_SERVER,
                SubsonicDataSource.ARG_USERNAME,
                SubsonicDataSource.ARG_PASSWORD,
                SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION,
            )

            JELLYFIN -> listOf(
                JellyfinDataSource.ARG_SERVER,
                JellyfinDataSource.ARG_USERNAME,
                JellyfinDataSource.ARG_PASSWORD,
            )
        }
    }
}
