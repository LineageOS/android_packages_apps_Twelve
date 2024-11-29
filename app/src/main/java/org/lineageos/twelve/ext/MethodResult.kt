/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import org.lineageos.twelve.datasources.MediaError
import org.lineageos.twelve.models.MethodResult
import org.lineageos.twelve.models.RequestStatus

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
    is MethodResult.InvalidResponse -> RequestStatus.Error(MediaError.INVALID_RESPONSE)
}

suspend fun <T, O> MethodResult<T>.toResult(
    resultGetter: suspend T.() -> O
): O? = when (this) {
    is MethodResult.Success -> result.resultGetter()
    is MethodResult.HttpError -> null
    is MethodResult.DeserializationError -> null
    is MethodResult.InvalidResponse -> null
}
