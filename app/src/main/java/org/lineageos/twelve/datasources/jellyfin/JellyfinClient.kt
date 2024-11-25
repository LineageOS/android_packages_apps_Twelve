/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.datasources.jellyfin

import android.net.Uri
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jellyfin.sdk.model.api.BaseItemDtoQueryResult
import org.lineageos.twelve.ext.executeAsync
import org.lineageos.twelve.models.SortingRule
import org.lineageos.twelve.models.SortingStrategy

class JellyfinClient(server: String, private val apiKey: String) {
    private val okHttpClient = OkHttpClient()

    private val serverUri = Uri.parse(server)

    suspend fun getAlbums(sortingRule: SortingRule) =
        method<BaseItemDtoQueryResult>("Artists/AlbumArtists", sortingRule)

    suspend fun getArtists(sortingRule: SortingRule) =
        method<BaseItemDtoQueryResult>("Artists", sortingRule)

    suspend fun getGenres(sortingRule: SortingRule) =
        method<BaseItemDtoQueryResult>("Genres", sortingRule)

    private suspend inline fun <reified T> method(
        method: String,
        sortingRule: SortingRule,
        vararg queryParameters: Pair<String, Any?>,
    ) = okHttpClient.newCall(
        Request.Builder()
            .url(getMethodUrl(method, sortingRule, *queryParameters))
            .headers(getBaseHeaders())
            .build()
    ).executeAsync().let { response ->
        when (response.isSuccessful) {
            true -> response.body?.string()?.let {
                MethodResult.Success(Json.decodeFromString<T>(it))
            } ?: throw Exception("Successful response with empty body")

            false -> MethodResult.HttpError(response.code)
        }
    }

    private fun getMethodUrl(
        method: String,
        sortingRule: SortingRule,
        vararg queryParameters: Pair<String, Any?>,
    ) = serverUri.buildUpon().apply {
        method.split("/").forEach {
            appendPath(it)
        }
        getBaseParameters().forEach { (key, value) ->
            appendQueryParameter(key, value)
        }
        queryParameters.forEach { (key, value) ->
            value?.let { appendQueryParameter(key, it.toString()) }
        }
        getSortParameter(sortingRule).forEach { (key, value) ->
            appendQueryParameter(key, value)
        }
    }.build().toString().also {
        println("URL: $it")
    }

    private fun getBaseHeaders() = Headers.Builder().apply {
        add("Authorization", "MediaBrowser Token=\"${apiKey}\"")
    }.build()

    private fun getSortParameter(sortingRule: SortingRule) = buildMap {
        put(
            "sortBy", when (sortingRule.strategy) {
                SortingStrategy.CREATION_DATE -> "DateCreated"
                SortingStrategy.MODIFICATION_DATE -> "DateLastContentAdded"
                SortingStrategy.NAME -> "Name"
                SortingStrategy.PLAY_COUNT -> "PlayCount"
            }
        )

        put(
            "sortOrder", when (sortingRule.reverse) {
                true -> "Descending"
                false -> "Ascending"
            }
        )
    }

    private fun getBaseParameters() = buildMap {
        put("mediaTypes", "Audio")
    }

    sealed interface MethodResult<T> {
        class Success<T>(val result: T) : MethodResult<T>
        class HttpError<T>(val code: Int) : MethodResult<T>
    }
}
