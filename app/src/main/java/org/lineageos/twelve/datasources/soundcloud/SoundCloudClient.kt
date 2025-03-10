/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud

import android.net.Uri
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.lineageos.twelve.datasources.soundcloud.models.User
import org.lineageos.twelve.utils.Api
import org.lineageos.twelve.utils.ApiRequest
import org.lineageos.twelve.utils.mapToError

/**
 * SoundCloud client.
 *
 * [API reference](https://developers.soundcloud.com/docs/api/explorer/open-api)
 *
 * @param cache OkHttp's [Cache]
 */
class SoundCloudClient(
    private val cache: Cache? = null,
) {
    private val okHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .build()

    private val api = Api(okHttpClient, SERVER_URI)

    /**
     * Returns the authenticated user's information.
     */
    suspend fun me() = ApiRequest.get<User>(
        listOf("me")
    ).execute(api).mapToError()

    companion object {
        private val SERVER_URI = Uri.Builder()
            .scheme("https")
            .authority("api.soundcloud.com")
            .build()
    }
}
