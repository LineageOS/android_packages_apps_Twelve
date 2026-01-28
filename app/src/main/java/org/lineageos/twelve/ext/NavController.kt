/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ext

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun NavController.navigateSafe(
    @IdRes id: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
) {
    currentDestination?.getAction(id)?.run {
        navigate(id, args, navOptions, navigatorExtras)
    }
}

/**
 * @see NavController.OnDestinationChangedListener.onDestinationChanged
 */
fun NavController.onDestinationChangedFlow() = callbackFlow {
    val onDestinationChangedListener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            trySend(Triple(controller, destination, arguments))
        }

    addOnDestinationChangedListener(onDestinationChangedListener)

    awaitClose {
        removeOnDestinationChangedListener(onDestinationChangedListener)
    }
}
