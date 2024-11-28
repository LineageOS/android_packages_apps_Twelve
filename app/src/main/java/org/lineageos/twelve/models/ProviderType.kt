/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.lineageos.twelve.R
import org.lineageos.twelve.datasources.MediaDataSource
import org.lineageos.twelve.datasources.SubsonicDataSource

/**
 * Data provider type. This regulates how data should be fetched, usually having a [MediaDataSource]
 * for each one.
 *
 * @param nameStringResId String resource ID of the display name of the provider
 * @param iconDrawableResId The drawable resource ID of the provider
 * @param arguments The arguments of the provider required to start a session. Those will be used
 *   by the providers manager to show the user a dialog to configure the provider
 */
enum class ProviderType(
    @StringRes val nameStringResId: Int,
    @DrawableRes val iconDrawableResId: Int,
    val arguments: List<ProviderArgument<*>>,
) {
    /**
     * Local provider.
     */
    LOCAL(
        R.string.provider_type_local,
        R.drawable.ic_shelves,
        listOf(),
    ),

    /**
     * Subsonic provider.
     *
     * [Home page](https://www.subsonic.org/pages/index.jsp)
     */
    SUBSONIC(
        R.string.provider_type_subsonic,
        R.drawable.ic_sailing,
        listOf(
            SubsonicDataSource.ARG_SERVER,
            SubsonicDataSource.ARG_USERNAME,
            SubsonicDataSource.ARG_PASSWORD,
            SubsonicDataSource.ARG_USE_LEGACY_AUTHENTICATION,
        ),
    ),
}
