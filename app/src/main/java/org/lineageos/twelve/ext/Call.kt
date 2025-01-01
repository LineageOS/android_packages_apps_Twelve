/*
 * SPDX-FileCopyrightText: 2022 Square, Inc.
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.internal.closeQuietly
import okio.IOException
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalCoroutinesApi::class) // resume with a resource cleanup.
suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    enqueue(
        object : Callback {
            override fun onResponse(
                call: Call,
                response: Response,
            ) {
                with(continuation) {
                    @Suppress("deprecation")
                    if (!isCancelled) resume(response) {
                        response.closeQuietly()
                    }
                }
            }

            override fun onFailure(
                call: Call,
                e: IOException,
            ) {
                with(continuation) {
                    if (!isCancelled) resumeWithException(e)
                }
            }
        },
    )

    continuation.invokeOnCancellation {
        cancel()
    }
}
