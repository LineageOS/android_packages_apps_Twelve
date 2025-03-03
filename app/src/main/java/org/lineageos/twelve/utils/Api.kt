/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.utils

import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.lineageos.twelve.ext.executeAsync
import org.lineageos.twelve.models.Error
import org.lineageos.twelve.models.Result
import java.net.SocketTimeoutException
import kotlin.reflect.KType
import kotlin.reflect.typeOf

typealias MethodResult<T> = Result<T, Error>

// Base interface for all API requests
interface ApiRequestInterface<T> {
    val type: KType
    suspend fun execute(api: Api): MethodResult<T>
}

// Base class for common request functionality
abstract class BaseRequest {
    protected fun encodeRequestBody(api: Api, data: Any?) = data?.let {
        api.json.encodeToString(it)
    }?.toRequestBody("application/json".toMediaType()) ?: "".toRequestBody()
}

// GET request implementation
class GetRequestInterface<T>(
    private val path: List<String>,
    override val type: KType,
    private val queryParameters: List<Pair<String, Any?>> = emptyList()
) : BaseRequest(), ApiRequestInterface<T> {
    override suspend fun execute(api: Api): MethodResult<T> {
        val url = api.buildUrl(path, queryParameters)
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        return api.executeRequest(request, type)
    }
}

// POST request implementation
class PostRequestInterface<T, E>(
    private val path: List<String>,
    override val type: KType,
    private val data: T?,
    private val queryParameters: List<Pair<String, Any?>> = emptyList(),
    private val emptyResponse: () -> E
) : BaseRequest(), ApiRequestInterface<E> {
    override suspend fun execute(api: Api): MethodResult<E> {
        val url = api.buildUrl(path, queryParameters)
        val body = encodeRequestBody(api, data)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        return api.executeRequest(request, type, emptyResponse)
    }
}

// DELETE request implementation
class DeleteRequestInterface<T>(
    private val path: List<String>,
    override val type: KType,
    private val queryParameters: List<Pair<String, Any?>> = emptyList()
) : BaseRequest(), ApiRequestInterface<T> {
    override suspend fun execute(api: Api): MethodResult<T> {
        val url = api.buildUrl(path, queryParameters)
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()
        return api.executeRequest(request, type)
    }
}

class Api(
    private val okHttpClient: OkHttpClient,
    private val serverUri: Uri,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    val json: Json = Json {
        ignoreUnknownKeys = true
    },
) {
    fun buildUrl(
        path: List<String>,
        queryParameters: List<Pair<String, Any?>> = emptyList()
    ) = serverUri.buildUpon().apply {
        path.forEach { appendPath(it) }
        queryParameters.forEach { (key, value) ->
            value?.let { appendQueryParameter(key, it.toString()) }
        }
    }.build().toString()

    suspend fun <T> executeRequest(
        request: Request,
        type: KType,
        onEmptyResponse: () -> T = {
            throw IllegalStateException("No onEmptyResponse() provided, but response is empty")
        }
    ) = withContext(dispatcher) {
        withRetry(maxAttempts = 3) {
            runCatching {
                okHttpClient.newCall(request).executeAsync().let { response ->
                    if (response.isSuccessful) {
                        response.body?.use { body ->
                            val string = body.string()
                            if (string.isEmpty()) {
                                MethodResult.Success(onEmptyResponse())
                            } else {
                                @Suppress("UNCHECKED_CAST")
                                val serializer =
                                    json.serializersModule.serializer(type) as KSerializer<T>
                                MethodResult.Success(json.decodeFromString(serializer, string))
                            }
                        } ?: MethodResult.Success(onEmptyResponse())
                    } else {
                        MethodResult.HttpError(response.code, Throwable(response.message))
                    }
                }
            }.fold(
                onSuccess = { it },
                onFailure = { e -> handleError(e) }
            )
        }
    }

    private suspend fun <T> withRetry(
        maxAttempts: Int,
        initialDelay: Long = 100,
        maxDelay: Long = 1000,
        factor: Double = 2.0,
        block: suspend () -> Result<T, Error>
    ): MethodResult<T> {
        var currentDelay = initialDelay
        repeat(maxAttempts - 1) { _ ->
            when (val result = block()) {
                is Result.Success -> return result
                is Result.Error -> when (result.error) {
                    in 500..599 -> {
                        delay(currentDelay)
                        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
                    }

                    else -> return result
                }

                else -> return result
            }
        }
        return block()
    }

    private fun <T> handleError(e: Throwable): MethodResult<T> = Result.Error(
        when (e) {
            is SocketTimeoutException -> Error.TIMEOUT
            is SerializationException -> Error.DESERIALIZATION
            is CancellationException -> Error.CANCELLED
            else -> Error.IO
        }
    )
}

object ApiRequest {
    inline fun <reified T> get(
        path: List<String>,
        queryParameters: List<Pair<String, Any?>> = emptyList()
    ) = GetRequestInterface<T>(path, typeOf<T>(), queryParameters)

    inline fun <reified T, reified E> post(
        path: List<String>,
        data: T? = null,
        queryParameters: List<Pair<String, Any?>> = emptyList(),
        noinline emptyResponse: () -> E = { Unit as E }
    ) = PostRequestInterface(path, typeOf<E>(), data, queryParameters, emptyResponse)

    inline fun <reified T> delete(
        path: List<String>,
        queryParameters: List<Pair<String, Any?>> = emptyList()
    ) = DeleteRequestInterface<T>(path, typeOf<T>(), queryParameters)
}

suspend fun <T, O> MethodResult<T>.toResult(
    resultGetter: suspend T.() -> O
) = when (this) {
    is Result.Success -> data.resultGetter()
    else -> null
}
