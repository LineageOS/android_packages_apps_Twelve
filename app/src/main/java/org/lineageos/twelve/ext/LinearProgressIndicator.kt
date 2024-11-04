/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import com.google.android.material.progressindicator.LinearProgressIndicator
import org.lineageos.twelve.models.RequestStatus

/**
 * @see LinearProgressIndicator.setProgressCompat
 */
fun <T, E> LinearProgressIndicator.setProgressCompat(
    status: RequestStatus<T, E>, animated: Boolean
) {
    when (status) {
        is RequestStatus.Loading -> {
            status.progress?.also {
                setProgressCompat(it, animated)
            } ?: run {
                if (!isIndeterminate) {
                    hide()
                    isIndeterminate = true
                }
            }

            show()
        }

        else -> {
            hide()
        }
    }
}
