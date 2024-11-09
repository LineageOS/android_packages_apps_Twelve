/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.ext.isLandscape
import org.lineageos.twelve.ext.updatePadding
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.ui.views.NowPlayingBar
import org.lineageos.twelve.viewmodels.NowPlayingViewModel
import org.lineageos.twelve.viewmodels.ProvidersViewModel

/**
 * The home page.
 */
class MainFragment : Fragment(R.layout.fragment_main) {
    // View models
    private val viewModel by viewModels<NowPlayingViewModel>()
    private val providersViewModel by viewModels<ProvidersViewModel>()

    // Views
    private val navigationBarView by getViewProperty<NavigationBarView>(R.id.navigationBarView)
    private val nowPlayingBar by getViewProperty<NowPlayingBar>(R.id.nowPlayingBar)
    private val providerMaterialButton by getViewProperty<MaterialButton>(R.id.providerMaterialButton)
    private val toolbar by getViewProperty<MaterialToolbar>(R.id.toolbar)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())

            v.updatePadding(
                insets,
                start = !resources.configuration.isLandscape,
                end = true,
            )

            windowInsets
        }

        if (resources.configuration.isLandscape) {
            ViewCompat.setOnApplyWindowInsetsListener(navigationBarView) { v, windowInsets ->
                // This is a navigation rail
                val insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                )

                v.updatePadding(
                    insets,
                    start = true,
                    top = true,
                    bottom = true,
                )

                windowInsets
            }
        }

        toolbar.setupWithNavController(findNavController())
        navigationBarView.setupWithNavController(findNavController())

        providerMaterialButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_mainFragment_to_fragment_provider_selector_dialog
            )
        }

        nowPlayingBar.setOnPlayPauseClickListener {
            viewModel.togglePlayPause()
        }

        nowPlayingBar.setOnNowPlayingClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_fragment_now_playing)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    providersViewModel.navigationProvider.collectLatest {
                        it?.let {
                            providerMaterialButton.text = it.name
                            providerMaterialButton.setIconResource(it.type.iconDrawableResId)
                        }
                    }
                }

                launch {
                    viewModel.durationCurrentPositionMs.collectLatest {
                        nowPlayingBar.updateDurationCurrentPositionMs(it.first, it.second)
                    }
                }

                launch {
                    viewModel.isPlaying.collectLatest {
                        nowPlayingBar.updateIsPlaying(it)
                    }
                }

                launch {
                    viewModel.mediaItem.collectLatest {
                        nowPlayingBar.updateMediaItem(it)
                    }
                }

                launch {
                    viewModel.mediaMetadata.collectLatest {
                        nowPlayingBar.updateMediaMetadata(it)
                    }
                }

                launch {
                    viewModel.mediaArtwork.collectLatest {
                        when (it) {
                            is RequestStatus.Loading -> {
                                // Do nothing
                            }

                            is RequestStatus.Success -> {
                                nowPlayingBar.updateMediaArtwork(it.data)
                            }

                            is RequestStatus.Error -> throw Exception(
                                "Error while getting media artwork"
                            )
                        }
                    }
                }
            }
        }
    }
}
