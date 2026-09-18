/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.viewmodels.PlaybackControlViewModel
import java.util.Locale

class PlaybackControlBottomSheetDialogFragment : TwelveBottomSheetDialogFragment(
    R.layout.fragment_playback_control_bottom_sheet_dialog
) {
    // View models
    private val viewModel by viewModels<PlaybackControlViewModel>()

    // Views
    private val playbackSpeedMaterialButton by getViewProperty<MaterialButton>(R.id.playbackSpeedMaterialButton)
    private val playbackSpeedMinusMaterialButton by getViewProperty<MaterialButton>(R.id.playbackSpeedMinusMaterialButton)
    private val playbackSpeedPlusMaterialButton by getViewProperty<MaterialButton>(R.id.playbackSpeedPlusMaterialButton)
    private val playbackPitchMaterialButton by getViewProperty<MaterialButton>(R.id.playbackPitchMaterialButton)
    private val playbackPitchMinusMaterialButton by getViewProperty<MaterialButton>(R.id.playbackPitchMinusMaterialButton)
    private val playbackPitchPlusMaterialButton by getViewProperty<MaterialButton>(R.id.playbackPitchPlusMaterialButton)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playbackSpeedMinusMaterialButton.setOnClickListener {
            viewModel.decreasePlaybackSpeed()
        }

        playbackSpeedMaterialButton.setOnClickListener {
            viewModel.resetPlaybackSpeed()
        }

        playbackSpeedPlusMaterialButton.setOnClickListener {
            viewModel.increasePlaybackSpeed()
        }

        playbackPitchMinusMaterialButton.setOnClickListener {
            viewModel.decreasePlaybackPitch()
        }

        playbackPitchMaterialButton.setOnClickListener {
            viewModel.resetPlaybackPitch()
        }

        playbackPitchPlusMaterialButton.setOnClickListener {
            viewModel.increasePlaybackPitch()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.playbackParameters.collectLatest {
                        playbackSpeedMaterialButton.text = getString(
                            R.string.playback_speed_format,
                            playbackSpeedFormatter.format(it.speed),
                        )
                        playbackPitchMaterialButton.text = getString(
                            R.string.playback_pitch_format,
                            playbackPitchFormatter.format(it.pitch),
                        )
                    }
                }

                launch {
                    viewModel.isSpeedMinusButtonEnabled.collectLatest {
                        playbackSpeedMinusMaterialButton.isEnabled = it
                    }
                }

                launch {
                    viewModel.isSpeedPlusButtonEnabled.collectLatest {
                        playbackSpeedPlusMaterialButton.isEnabled = it
                    }
                }

                launch {
                    viewModel.isPitchMinusButtonEnabled.collectLatest {
                        playbackPitchMinusMaterialButton.isEnabled = it
                    }
                }

                launch {
                    viewModel.isPitchPlusButtonEnabled.collectLatest {
                        playbackPitchPlusMaterialButton.isEnabled = it
                    }
                }
            }
        }
    }

    companion object {
        private val decimalFormatSymbols = DecimalFormatSymbols(Locale.ROOT)
        private val playbackSpeedFormatter = DecimalFormat("0.#", decimalFormatSymbols)
        private val playbackPitchFormatter = DecimalFormat("0.#", decimalFormatSymbols)
    }
}
