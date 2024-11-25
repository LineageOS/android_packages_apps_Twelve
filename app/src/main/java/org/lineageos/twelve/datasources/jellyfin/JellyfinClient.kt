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

class JellyfinClient(private val server: String, private val apiKey: String) {
    private val okHttpClient = OkHttpClient()

    private val serverUri = Uri.parse(server)

    suspend fun getArtists(sortingRule: SortingRule) =
        method<BaseItemDtoQueryResult>("Artists", sortingRule)

    suspend fun getGenres(sortingRule: SortingRule) =
        method<BaseItemDtoQueryResult>("Genres", sortingRule)

    suspend fun getPlaylists(sortingRule: SortingRule) =
        method<BaseItemDtoQueryResult>("Playlists", sortingRule)

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
        appendPath(method)
        queryParameters.forEach { (key, value) ->
            value?.let { appendQueryParameter(key, it.toString()) }
        }
        getSortParameter(sortingRule).forEach { (key, value) ->
            appendQueryParameter(key, value)
        }
    }.build().toString()

    private fun getBaseHeaders() = Headers.Builder().apply {
        add("Authorization", apiKey)
    }.build()

    private fun getSortParameter(sortingRule: SortingRule) = buildMap<String, String> {
        when (sortingRule.strategy) {
            SortingStrategy.MODIFICATION_DATE -> put("sortBy", "DateCreated")
            SortingStrategy.NAME -> put("sortBy", "SortName")
            SortingStrategy.PLAY_COUNT -> put("sortBy", "PlayCount")
            SortingStrategy.CREATION_DATE -> put("sortBy", "DateCreated")
        }

        when (sortingRule.reverse) {
            true -> put("sortOrder", "Ascending")
            false -> put("sortOrder", "Descending")
        }
    }

    sealed interface MethodResult<T> {
        class Success<T>(val result: T) : MethodResult<T>
        class HttpError<T>(val code: Int) : MethodResult<T>
    }
}
