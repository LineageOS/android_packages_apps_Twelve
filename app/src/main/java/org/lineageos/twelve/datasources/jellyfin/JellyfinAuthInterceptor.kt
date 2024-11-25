/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.jellyfin

import android.content.SharedPreferences
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import org.lineageos.twelve.ext.jellyfinToken

class JellyfinAuthInterceptor(
    private val sharedPreferences: SharedPreferences,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // If no token is found, simply proceed with the JellyfinAuthenticator
        val token = sharedPreferences.jellyfinToken
            ?: return chain.proceed(chain.request())

        val request = chain.request().newBuilder()
            .headers(getAuthHeaders(token))
            .build()

        return chain.proceed(request)
    }

    private fun getAuthHeaders(token: String) = Headers.Builder().apply {
        add("Authorization", "MediaBrowser Token=\"${token}\"")
    }.build()
}
