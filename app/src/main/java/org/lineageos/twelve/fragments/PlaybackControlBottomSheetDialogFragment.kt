/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.viewmodels.PlaybackControlViewModel
import java.util.Locale

class PlaybackControlBottomSheetDialogFragment : BottomSheetDialogFragment(
    R.layout.fragment_playback_control_dialog
) {
    // View models
    private val viewModel by viewModels<PlaybackControlViewModel>()

    // Views
    private val playbackSpeedPlusButton by getViewProperty<Button>(R.id.playbackSpeedPlusButton)
    private val playbackSpeedMaterialButton by getViewProperty<MaterialButton>(R.id.playbackSpeedMaterialButton)
    private val playbackSpeedMinusButton by getViewProperty<Button>(R.id.playbackSpeedMinusButton)
    private val playbackPitchUnlockSwitch by getViewProperty<Button>(R.id.playbackPitchUnlockSwitch)
    private val playbackPitchSlider by getViewProperty<Slider>(R.id.playbackPitchSlider)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playbackSpeedMinusButton.setOnClickListener {
            viewModel.decreasePlaybackSpeed()
        }

        playbackSpeedMaterialButton.setOnClickListener {
            viewModel.resetPlaybackSpeed()
        }

        playbackSpeedPlusButton.setOnClickListener {
            viewModel.increasePlaybackSpeed()
        }

        playbackPitchUnlockSwitch.setOnClickListener {
            viewModel.togglePitchUnlock()
        }

        val sliderFrom = playbackPitchSlider.valueFrom
        val sliderTo = playbackPitchSlider.valueTo

        // Range must odd length to ensure we have a center value
        require(
            (sliderTo - sliderFrom).toInt() % 2 == 0
        ) { "Slider range must have an odd length" }

        playbackPitchSlider.addOnChangeListener { _, value, _ ->
            viewModel.setPlaybackPitch(
                PlaybackControlViewModel.sliderToPitch(
                    value,
                    sliderFrom,
                    sliderTo
                )
            )
        }

        playbackPitchSlider.setLabelFormatter {
            playbackPitchFormatter.format(
                PlaybackControlViewModel.sliderToPitch(
                    it,
                    sliderFrom,
                    sliderTo
                )
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.playbackParameters.collectLatest {
                        playbackSpeedMaterialButton.text = getString(
                            R.string.playback_speed_format,
                            playbackSpeedFormatter.format(it.speed),
                        )
                        playbackPitchSlider.value =
                            PlaybackControlViewModel.pitchToSlider(it.pitch, sliderFrom, sliderTo)
                    }
                }

                launch {
                    viewModel.speedMinusButtonEnabled.collectLatest {
                        playbackSpeedMinusButton.isEnabled = it
                    }
                }

                launch {
                    viewModel.speedPlusButtonEnabled.collectLatest {
                        playbackSpeedPlusButton.isEnabled = it
                    }
                }

                launch {
                    viewModel.pitchSliderVisible.collectLatest {
                        playbackPitchSlider.isVisible = it
                    }
                }
            }
        }
    }

    companion object {
        private val decimalFormatSymbols = DecimalFormatSymbols(Locale.ROOT)
        private val playbackSpeedFormatter = DecimalFormat("0.##", decimalFormatSymbols)
        private val playbackPitchFormatter = DecimalFormat("0.#", decimalFormatSymbols)
    }
}
