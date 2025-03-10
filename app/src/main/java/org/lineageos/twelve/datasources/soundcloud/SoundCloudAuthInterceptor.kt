/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud

import okhttp3.Interceptor

class SoundCloudAuthInterceptor(
    private val clientId: String?,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain) = clientId?.let { clientId ->
        chain.proceed(
            chain.request().newBuilder()
                .url(
                    chain.request().url.newBuilder()
                        .addQueryParameter("client_id", clientId)
                        .build()
                )
                .addHeader("Origin", "https://soundcloud.com")
                .build()
        )
    } ?: chain.proceed(chain.request())
}
