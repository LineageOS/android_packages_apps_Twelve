/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.audiobookshelf

import android.net.Uri
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.lineageos.twelve.datasources.audiobookshelf.models.AudioFile
import org.lineageos.twelve.datasources.audiobookshelf.models.Item
import org.lineageos.twelve.datasources.audiobookshelf.models.Libraries
import org.lineageos.twelve.datasources.audiobookshelf.models.LibraryItems
import org.lineageos.twelve.utils.Api
import org.lineageos.twelve.utils.ApiRequest

/**
 * Jellyfin client.
 *
 * @param server The base URL of the server
 * @param username The login username of the server
 * @param password The corresponding password of the user
 * @param tokenGetter A function to get the token
 * @param tokenSetter A function to set the token
 * @param cache OkHttp's [Cache]
 */
class AudiobookshelfClient(
    server: String,
    private val username: String,
    private val password: String,
    tokenGetter: () -> String?,
    tokenSetter: (String) -> Unit,
    cache: Cache? = null,
) {
    private val serverUri = Uri.parse(server)

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AudiobookshelfAuthInterceptor(tokenGetter))
        .authenticator(
            AudiobookshelfAuthenticator(
                serverUri,
                username,
                password,
                tokenGetter,
                tokenSetter,
            )
        )
        .cache(cache)
        .build()

    private val apiUri = serverUri.buildUpon()
        .appendPath("api")
        .build()

    private val api = Api(okHttpClient, apiUri)

    suspend fun getLibraries() = ApiRequest.get<Libraries>(
        listOf("libraries"),
    ).execute(api)

    suspend fun getLibraryItems(id: String) = ApiRequest.get<LibraryItems>(
        listOf("libraries", id, "items"),
    ).execute(api)

    suspend fun getItem(id: String) = ApiRequest.get<Item>(
        listOf("items", id),
    ).execute(api)

    fun getPlaybackUrl(audioFile: AudioFile) = api.buildUrl(
        listOf(
            audioFile.metadata.path
        ),
    )

    fun getCover(id: String) = api.buildUrl(
        listOf(
            "items",
            id,
            "cover",
        ),
    )
}
