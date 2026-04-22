/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import java.util.concurrent.Executors
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun StorageManager.storageVolumesFlow() = callbackFlow {
    val update = { trySend(storageVolumes) }

    val storageVolumeCallback =
        object : StorageManager.StorageVolumeCallback() {
            override fun onStateChanged(volume: StorageVolume) {
                update()
            }
        }

    registerStorageVolumeCallback(Executors.newSingleThreadExecutor(), storageVolumeCallback)

    update()

    awaitClose { unregisterStorageVolumeCallback(storageVolumeCallback) }
}
