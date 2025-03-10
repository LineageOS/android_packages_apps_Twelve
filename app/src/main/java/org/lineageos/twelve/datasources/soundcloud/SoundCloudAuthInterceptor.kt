/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud

import okhttp3.Interceptor

class SoundCloudAuthInterceptor(
    private val clientId: String,
    private val oAuthToken: String?,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain) = chain.proceed(
        chain.request().newBuilder()
            .url(
                chain.request().url.newBuilder()
                    .apply {
                        getAuthParameters().forEach { (key, value) ->
                            addQueryParameter(key, value)
                        }
                    }
                    .build()
            )
            .apply {
                oAuthToken?.let {
                    addHeader("Authorization", "OAuth $oAuthToken")
                }
            }
            .build()
    )

    fun getAuthParameters() = listOf(
        "client_id" to clientId,
    )
}
