/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.audiobookshelf

import android.net.Uri
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
import org.lineageos.twelve.datasources.audiobookshelf.models.AuthenticateUser
import org.lineageos.twelve.datasources.audiobookshelf.models.AuthenticateUserResult
import org.lineageos.twelve.ext.executeAsync

class AudiobookshelfAuthenticator(
    serverUri: Uri,
    private val username: String,
    private val password: String,
    private val tokenGetter: () -> String?,
    private val tokenSetter: (String) -> Unit,
) : Authenticator {
    private val mutex = Mutex()
    private val okHttpClient = OkHttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val authenticationUrl = serverUri.buildUpon()
        .appendPath("login")
        .build()
        .toString()

    // This block is only run in case the request got a 401 error code
    override fun authenticate(route: Route?, response: Response) = runBlocking {
        val token = tokenGetter()

        mutex.withLock {
            val newToken = tokenGetter()

            // Ensure no other request has updated the token
            // If it did we assume the new token is valid
            if (newToken != null && newToken != token) {
                return@runBlocking response.request.newBuilder()
                    .headers(getAuthHeaders(newToken))
                    .build()
            }

            // Either the previous token expired or we didn't have a token
            getNewAccessToken()?.let {
                tokenSetter(it)

                return@runBlocking response.request.newBuilder()
                    .headers(getAuthHeaders(it))
                    .build()
            }
        }

        // If we reach this point, we couldn't get a new token
        null
    }

    private fun getNewAccessToken() = runBlocking {
        val response = runCatching {
            okHttpClient.newCall(
                Request.Builder()
                    .url(authenticationUrl)
                    .post(
                        json.encodeToString(
                            AuthenticateUser(username, password)
                        ).toRequestBody("application/json".toMediaType())
                    )
                    .build()
            ).executeAsync()
        }.fold(
            onSuccess = { it },
            onFailure = { return@runBlocking null }
        )

        if (!response.isSuccessful) {
            return@runBlocking null
        }

        val authResponse = response.body?.use { body ->
            json.decodeFromString<AuthenticateUserResult>(body.string())
        } ?: return@runBlocking null

        authResponse.user.token
    }

    private fun getAuthHeaders(token: String) = Headers.Builder().apply {
        add("Authorization", "Bearer \"${token}\"")
    }.build()
}
