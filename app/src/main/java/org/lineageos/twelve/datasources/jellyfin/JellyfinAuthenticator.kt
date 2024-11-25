/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.jellyfin

import android.net.Uri
import android.os.Build
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.jellyfin.sdk.model.UUID
import org.jellyfin.sdk.model.api.AuthenticateUserByName
import org.jellyfin.sdk.model.api.AuthenticationResult
import org.lineageos.twelve.ext.executeAsync

class JellyfinAuthenticator(
    private val mutex: Mutex,
    serverUri: Uri,
    private val username: String,
    private val password: String,
    private val deviceIdentifier: String,
    private val packageName: String
) : Authenticator {
    private val okHttpClient = OkHttpClient()
    private lateinit var accessToken: String
    lateinit var userId: UUID

    private val authenticationUrl = serverUri.buildUpon()
        .appendPath("Users")
        .appendPath("AuthenticateByName")
        .build()
        .toString()

    override fun authenticate(route: Route?, response: Response) = runBlocking {
        mutex.withLock {
            if (!isTokenExpired()) {
                return@runBlocking response.request.newBuilder()
                    .headers(getAuthHeaders(accessToken))
                    .build()
            }

            accessToken = getNewAccessToken()

            response.request.newBuilder()
                .headers(getAuthHeaders(accessToken))
                .build()
        }
    }

    private fun isTokenExpired() = this::accessToken.isInitialized.not()

    private fun getNewAccessToken() = runBlocking {
        val response = okHttpClient.newCall(
            Request.Builder()
                .url(authenticationUrl)
                .headers(getAuthenticationRequestHeaders())
                .post(
                    Json.encodeToString<AuthenticateUserByName>(
                        AuthenticateUserByName(username, password)
                    ).toRequestBody("application/json".toMediaType())
                )
                .build()
        ).executeAsync()

        if (!response.isSuccessful) {
            throw Exception("Authentication failed with code ${response.code}")
        }

        val authResponse = response.body?.use { body ->
            Json.decodeFromString<AuthenticationResult>(body.string())
        } ?: throw Exception("Empty authentication response")

        userId = authResponse.user!!.id
        accessToken = authResponse.accessToken!!

        accessToken
    }

    private fun getAuthenticationRequestHeaders() = Headers.Builder().apply {
        add(
            "X-Emby-Authorization",
            "MediaBrowser Client=\"${packageName}\", " +
                    "Device=\"${Build.MODEL}\", " +
                    "DeviceId=\"${deviceIdentifier}\", " +
                    "Version=\"$JELLYFIN_API_VERSION\""
        )
    }.build()

    private fun getAuthHeaders(accessToken: String) = Headers.Builder().apply {
        add("Authorization", "MediaBrowser Token=\"${accessToken}\"")
    }.build()

    companion object {
        private const val JELLYFIN_API_VERSION = "10.10.3"
    }
}
