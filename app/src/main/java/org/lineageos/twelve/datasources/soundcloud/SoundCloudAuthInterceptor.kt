/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.soundcloud

import okhttp3.Interceptor

class SoundCloudAuthInterceptor(
    private val clientId: String?,
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
            .build()
    )

    fun getAuthParameters() = buildList {
        clientId?.let {
            add("client_id" to it)
        }
    }
}
