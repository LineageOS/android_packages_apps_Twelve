/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.audiobookshelf

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response

class AudiobookshelfAuthInterceptor(
    private val tokenGetter: () -> String?,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // If no token is found, simply proceed with the JellyfinAuthenticator
        val token = tokenGetter() ?: return chain.proceed(chain.request())

        val request = chain.request().newBuilder()
            .headers(getAuthHeaders(token))
            .build()

        return chain.proceed(request)
    }

    private fun getAuthHeaders(token: String) = Headers.Builder().apply {
        add("Authorization", "Bearer $token")
    }.build()
}
