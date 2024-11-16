/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import coil3.ImageLoader
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.Disposable
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.request.target

inline fun ImageView.loadThumbnail(
    uri: Uri,
    imageLoader: ImageLoader = context.imageLoader,
    @DrawableRes placeholder: Int? = null,
    builder: ImageRequest.Builder.() -> Unit = {
        // Skip crossfade if image is already in memory cache
        if (imageLoader.memoryCache?.get(MemoryCache.Key(uri.toString())) != null) {
            crossfade(false)
        } else {
            crossfade(true)
        }
        placeholder?.let {
            placeholder(it)
            error(it)
        }
    },
): Disposable {
    val request = ImageRequest.Builder(context)
        .data(uri)
        .target(this)
        .apply(builder)
        .build()
    return imageLoader.enqueue(request)
}
