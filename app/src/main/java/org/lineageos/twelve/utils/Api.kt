/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import android.net.Uri
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.lineageos.twelve.datasources.MediaError
import org.lineageos.twelve.ext.executeAsync
import org.lineageos.twelve.models.RequestStatus

class Api(val okHttpClient: OkHttpClient, private val serverUri: Uri) {

    // GET method
    suspend inline fun <reified T> get(
        path: List<String>,
        queryParameters: List<Pair<String, Any>> = emptyList(),
    ): MethodResult<T> {
        val url = buildUrl(path, queryParameters)

        val request = Request.Builder()
            .url(url)
            .build()

        return executeRequest<T>(request)
    }

    // POST method
    suspend inline fun <reified T, reified E> post(
        path: List<String>,
        queryParameters: List<Pair<String, Any>> = emptyList(),
        data: T? = null,
        emptyResponse: () -> E = { Unit as E }
    ): MethodResult<E> {
        val url = buildUrl(path, queryParameters)

        val requestBody = (data?.let {
            Json.encodeToString<T>(it)
        } ?: "").toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return executeRequest<E>(request, emptyResponse)
    }

    // DELETE method
    suspend inline fun <reified T> delete(
        path: List<String>,
        queryParameters: List<Pair<String, Any>> = emptyList(),
    ): MethodResult<T> {
        val url = buildUrl(path, queryParameters)

        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        return executeRequest(request) { Unit as T }
    }

    // Helper function to build the URL
    fun buildUrl(
        path: List<String>,
        queryParameters: List<Pair<String, Any>> = emptyList()
    ) = serverUri.buildUpon().apply {
        path.forEach {
            appendPath(it)
        }
        queryParameters.forEach { (key, value) ->
            appendQueryParameter(key, value.toString())
        }
    }.build().toString().also {
        println("Built URL: $it")
    }

    // Helper function to execute the request
    suspend inline fun <reified T> executeRequest(
        request: Request,
        emptyResponse: () -> T = {
            throw IllegalStateException("No empty response provided")
        }
    ) = okHttpClient.newCall(request).executeAsync().let { response ->
        if (response.isSuccessful) {
            response.body?.use { body ->
                val string = body.string()
                if (string.isEmpty()) {
                    MethodResult.Success(emptyResponse())
                } else {
                    runCatching {
                        MethodResult.Success(Json.decodeFromString<T>(string))
                    }.getOrElse {
                        MethodResult.DeserializationError()
                    }
                }
            } ?: throw Exception("Successful response with no body")
        } else {
            MethodResult.HttpError(response.code)
        }
    }
}

sealed interface MethodResult<T> {
    class Success<T>(val result: T) : MethodResult<T>
    class HttpError<T>(val code: Int) : MethodResult<T>
    class DeserializationError<T> : MethodResult<T>
}

suspend fun <T, O> MethodResult<T>.toRequestStatus(
    resultGetter: suspend T.() -> O
): RequestStatus<O, MediaError> = when (this) {
    is MethodResult.Success -> RequestStatus.Success(result.resultGetter())

    is MethodResult.HttpError -> RequestStatus.Error(
        when (code) {
            401 -> MediaError.AUTHENTICATION_REQUIRED
            403 -> MediaError.INVALID_CREDENTIALS
            404 -> MediaError.NOT_FOUND
            else -> MediaError.IO
        }
    )

    is MethodResult.DeserializationError -> RequestStatus.Error(MediaError.DESERIALIZATION)
}

suspend fun <T, O> MethodResult<T>.toResult(
    resultGetter: suspend T.() -> O
): O? = when (this) {
    is MethodResult.Success -> result.resultGetter()
    is MethodResult.HttpError -> null
    is MethodResult.DeserializationError -> null
}
