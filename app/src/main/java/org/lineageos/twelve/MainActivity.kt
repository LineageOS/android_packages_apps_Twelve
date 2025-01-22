/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.ext.defaultProvider
import org.lineageos.twelve.ext.navigateSafe
import org.lineageos.twelve.fragments.AlbumFragment
import org.lineageos.twelve.fragments.ArtistFragment
import org.lineageos.twelve.fragments.PlaylistFragment
import org.lineageos.twelve.models.MediaType
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.viewmodels.IntentsViewModel
import org.lineageos.twelve.viewmodels.ProvidersViewModel
import kotlin.reflect.cast

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    // View models
    private val intentsViewModel by viewModels<IntentsViewModel>()
    private val viewModel by viewModels<ProvidersViewModel>()

    // NavController
    private val navHostFragment by lazy {
        NavHostFragment::class.cast(
            supportFragmentManager.findFragmentById(R.id.navHostFragment)
        )
    }
    private val navController by lazy { navHostFragment.navController }

    // Intents
    private val intentListener = Consumer<Intent> { intentsViewModel.onIntent(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        enableEdgeToEdge()

        intentsViewModel.onIntent(intent)
        addOnNewIntentListener(intentListener)

        // Restore provider from previous session.
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.providers.collect { requestStatus ->
                    when (requestStatus) {
                        is RequestStatus.Loading -> {
                            // Do nothing
                        }

                        is RequestStatus.Success -> {
                            val sharedPreferences =
                                PreferenceManager.getDefaultSharedPreferences(application)!!
                            val defaultProvider = sharedPreferences.defaultProvider
                            requestStatus.data.firstOrNull { defaultProvider.matches(it) }?.let {
                                viewModel.setNavigationProvider(it)
                            }
                        }

                        is RequestStatus.Error -> throw Exception("Error while loading providers")
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                intentsViewModel.parsedIntent.collectLatest { parsedIntent ->
                    parsedIntent?.handle {
                        when (it.action) {
                            IntentsViewModel.ParsedIntent.Action.MAIN -> {
                                // We don't need to do anything
                            }

                            IntentsViewModel.ParsedIntent.Action.OPEN_NOW_PLAYING -> {
                                navController.navigateSafe(R.id.fragment_now_playing)
                            }

                            IntentsViewModel.ParsedIntent.Action.VIEW -> {
                                if (it.contents.isEmpty()) {
                                    Log.i(LOG_TAG, "No content to view")
                                    return@handle
                                }

                                val isSingleItem = it.contents.size == 1
                                if (!isSingleItem) {
                                    Log.i(LOG_TAG, "Cannot handle multiple items")
                                    return@handle
                                }

                                val content = it.contents.first()

                                when (content.type) {
                                    MediaType.ALBUM -> navController.navigateSafe(
                                        R.id.fragment_album,
                                        AlbumFragment.createBundle(content.uri)
                                    )

                                    MediaType.ARTIST -> navController.navigateSafe(
                                        R.id.fragment_artist,
                                        ArtistFragment.createBundle(content.uri)
                                    )

                                    MediaType.AUDIO -> Log.i(LOG_TAG, "Audio not supported")

                                    MediaType.GENRE -> Log.i(LOG_TAG, "Genre not supported")

                                    MediaType.PLAYLIST -> navController.navigateSafe(
                                        R.id.fragment_playlist,
                                        PlaylistFragment.createBundle(content.uri)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        removeOnNewIntentListener(intentListener)

        super.onDestroy()
    }

    companion object {
        private val LOG_TAG = MainActivity::class.simpleName!!

        /**
         * Open now playing fragment.
         * Type: [Boolean]
         */
        const val EXTRA_OPEN_NOW_PLAYING = "extra_now_playing"
    }
}
