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
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.lineageos.twelve.datasources.MediaError
import org.lineageos.twelve.ext.executeAsync
import org.lineageos.twelve.models.RequestStatus

class Api(val okHttpClient: OkHttpClient, private val serverUri: Uri) {
    val json = Json {
        ignoreUnknownKeys = true
    }

    // GET method
    suspend inline fun <reified T> get(
        path: List<String>,
        queryParameters: List<Pair<String, Any>> = emptyList(),
    ): MethodResult<T> {
        val url = buildUrl(path, queryParameters)
        val request = buildRequest(url, "GET")
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
        val body = encodeRequestBody(data)
        val request = buildRequest(url, "POST", body)
        return executeRequest<E>(request, emptyResponse)
    }

    // DELETE method
    suspend inline fun <reified T> delete(
        path: List<String>,
        queryParameters: List<Pair<String, Any>> = emptyList(),
    ): MethodResult<T> {
        val url = buildUrl(path, queryParameters)
        val request = buildRequest(url, "DELETE")
        return executeRequest(request) { Unit as T }
    }

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
    }.build().toString()

    inline fun <reified T> encodeRequestBody(data: T?) = data?.let {
        json.encodeToString(it)
    }?.toRequestBody("application/json".toMediaType()) ?: "".toRequestBody()

    fun buildRequest(url: String, method: String, body: RequestBody? = null) =
        Request.Builder().url(url).method(method, body).build()

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
                        json.decodeFromString<T>(string)
                    }.fold(
                        onSuccess = { MethodResult.Success(it) },
                        onFailure = { MethodResult.DeserializationError(it) }
                    )
                }
            } ?: MethodResult.Success(emptyResponse())
        } else {
            MethodResult.HttpError(response.code, response.message)
        }
    }
}

sealed interface MethodResult<T> {
    data class Success<T>(val result: T) : MethodResult<T>
    data class HttpError<T>(val code: Int, val message: String? = null) : MethodResult<T>
    class DeserializationError<T>(val error: Throwable? = null) : MethodResult<T>
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
