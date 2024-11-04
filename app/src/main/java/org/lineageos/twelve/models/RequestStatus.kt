/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.models

/**
 * Request status for flows.
 */
sealed class RequestStatus<T> {
    /**
     * Result is not ready yet.
     *
     * @param progress An optional percentage of the request progress
     */
    class Loading<T>(
        @androidx.annotation.IntRange(from = 0, to = 100) val progress: Int? = null
    ) : RequestStatus<T>()

    /**
     * The result is ready.
     *
     * @param data The obtained data
     */
    class Success<T>(val data: T) : RequestStatus<T>()

    /**
     * The request failed.
     *
     * @param error The error
     */
    class Error<T>(val error: Type) : RequestStatus<T>() {
        enum class Type {
            /**
             * This feature isn't implemented.
             */
            NOT_IMPLEMENTED,

            /**
             * I/O error, can also be network.
             */
            IO,

            /**
             * Authentication error.
             */
            AUTHENTICATION_REQUIRED,

            /**
             * Invalid credentials.
             */
            INVALID_CREDENTIALS,

            /**
             * The item was not found.
             */
            NOT_FOUND,

            /**
             * Value returned on write requests: The value already exists.
             */
            ALREADY_EXISTS,
        }
    }
}
