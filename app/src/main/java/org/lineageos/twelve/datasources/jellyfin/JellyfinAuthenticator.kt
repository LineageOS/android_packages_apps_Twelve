/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.jellyfin

import android.content.SharedPreferences
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
import org.lineageos.twelve.datasources.jellyfin.models.AuthenticateUser
import org.lineageos.twelve.datasources.jellyfin.models.AuthenticateUserResult
import org.lineageos.twelve.ext.executeAsync
import org.lineageos.twelve.ext.jellyfinToken

class JellyfinAuthenticator(
    private val sharedPreferences: SharedPreferences,
    serverUri: Uri,
    private val username: String,
    private val password: String,
    private val deviceIdentifier: String,
    private val packageName: String,
) : Authenticator {
    private val mutex = Mutex()
    private val okHttpClient = OkHttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val authenticationUrl = serverUri.buildUpon()
        .appendPath("Users")
        .appendPath("AuthenticateByName")
        .build()
        .toString()

    override fun authenticate(route: Route?, response: Response) = runBlocking {
        val token = sharedPreferences.jellyfinToken

        mutex.withLock {
            val newToken = sharedPreferences.jellyfinToken

            if (newToken != null && newToken != token) {
                return@runBlocking response.request.newBuilder()
                    .headers(getAuthHeaders(newToken))
                    .build()
            }

            println("Response code: ${response.code}")

            // This block is only run in case the request got a 401 error code
            // Either the previous token expire or we didn't have a token
            // In Jellyfin case we must regenerate a new token
            getNewAccessToken().let {
                println("New token: $it")

                sharedPreferences.jellyfinToken = it

                response.request.newBuilder()
                    .headers(getAuthHeaders(it))
                    .build()
            }
        }
    }

    private fun getNewAccessToken() = runBlocking {
        val response = okHttpClient.newCall(
            Request.Builder()
                .url(authenticationUrl)
                .headers(getAuthenticationRequestHeaders())
                .post(
                    json.encodeToString(
                        AuthenticateUser(username, password)
                    ).toRequestBody("application/json".toMediaType())
                )
                .build()
        ).executeAsync()

        if (!response.isSuccessful) {
            throw Exception("Authentication failed with code ${response.code}")
        }

        val authResponse = response.body?.use { body ->
            json.decodeFromString<AuthenticateUserResult>(body.string())
        } ?: throw Exception("Empty authentication response")

        authResponse.accessToken!!
    }

    private fun getAuthenticationRequestHeaders() = Headers.Builder().apply {
        add(
            "X-Emby-Authorization",
            "MediaBrowser Client=\"${packageName}\", " +
                    "Device=\"${Build.MODEL}\", " +
                    "DeviceId=\"${deviceIdentifier}\", " +
                    "Version=\"${JellyfinClient.JELLYFIN_API_VERSION}\""
        )
    }.build()

    private fun getAuthHeaders(token: String) = Headers.Builder().apply {
        add("Authorization", "MediaBrowser Token=\"${token}\"")
    }.build()
}
