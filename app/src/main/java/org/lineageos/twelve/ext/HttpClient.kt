/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.buildUrl
import io.ktor.http.contentType
import org.lineageos.twelve.models.MethodResult

suspend inline fun <reified T> HttpClient.get(
    path: List<String>,
    queryParameters: List<Pair<String, Any?>> = emptyList(),
) = runCatching {
    get {
        url {
            appendPathSegments(path)
            queryParameters.forEach { (key, value) ->
                parameters.append(key, value.toString())
            }
        }
    }.body<T>()
}.fold(
    onSuccess = { MethodResult.Success(it) },
    onFailure = {
        MethodResult.DeserializationError(it)
    }
)

suspend inline fun <reified T> HttpClient.post(
    path: List<String>,
    queryParameters: List<Pair<String, Any?>> = emptyList(),
    body: Any? = null,
) = runCatching {
    post {
        url {
            appendPathSegments(path)
            queryParameters.forEach { (key, value) ->
                parameters.append(key, value.toString())
            }
        }
        contentType(ContentType.Application.Json)
        setBody(body)
    }.body<T>()
}.fold(
    onSuccess = { MethodResult.Success(it) },
    onFailure = {
        MethodResult.DeserializationError(it)
    }
)

suspend inline fun <reified T> HttpClient.delete(
    path: List<String>,
    queryParameters: List<Pair<String, Any?>> = emptyList(),
) = runCatching {
    delete {
        url {
            appendPathSegments(path)
            queryParameters.forEach { (key, value) ->
                parameters.append(key, value.toString())
            }
        }
    }.body<T>()
}.fold(
    onSuccess = { MethodResult.Success(it) },
    onFailure = {
        MethodResult.DeserializationError(it)
    }
)

fun HttpClient.buildUrl(
    path: List<String>,
    queryParameters: List<Pair<String, Any?>> = emptyList(),
) = buildUrl {
    appendPathSegments(path)
    queryParameters.forEach { (key, value) ->
        parameters.append(key, value.toString())
    }
}.toString()
