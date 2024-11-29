/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

import org.lineageos.twelve.datasources.MediaError

sealed interface MethodResult<T> {
    data class Success<T>(val result: T) : MethodResult<T>
    data class HttpError<T>(val code: Int, val message: String? = null) : MethodResult<T>
    class DeserializationError<T>(val error: Throwable? = null) : MethodResult<T>
    data class InvalidResponse<T>(val error: Throwable? = null) : MethodResult<T>
}
