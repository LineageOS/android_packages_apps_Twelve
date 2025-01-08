/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.fragments

import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.R
import org.lineageos.twelve.ext.getViewProperty
import org.lineageos.twelve.viewmodels.PlaybackControlViewModel
import java.util.Locale

class PlaybackControlDialogFragment : DialogFragment(R.layout.fragment_playback_control_dialog) {
    // View models
    private val viewModel by viewModels<PlaybackControlViewModel>()

    // Views
    private val playbackSpeedSlider by getViewProperty<Slider>(R.id.playbackSpeedSlider)
    private val playbackSpeedLabel by getViewProperty<TextView>(R.id.playbackSpeedLabel)
    private val playbackPitchSlider by getViewProperty<Slider>(R.id.playbackPitchSlider)
    private val playbackPitchLabel by getViewProperty<TextView>(R.id.playbackPitchLabel)

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(
        requireContext()
    ).show()!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playbackSpeedSlider.addOnChangeListener { _, value, _ ->
            viewModel.setPlaybackSpeed(value / 10)
        }
        playbackPitchSlider.setLabelFormatter {
            playbackControlFormatter.format(it.toDouble() / 10)
        }

        playbackPitchSlider.addOnChangeListener { _, value, _ ->
            viewModel.setPlaybackPitch(value / 10)
        }
        playbackPitchSlider.setLabelFormatter {
            playbackControlFormatter.format(it.toDouble() / 10)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.playbackParameters.collectLatest { playbackParameters ->
                        playbackSpeedSlider.value = playbackParameters.speed * 10
                        playbackPitchSlider.value = playbackParameters.pitch * 10
                    }
                }
            }
        }
    }

    companion object {
        private val decimalFormatSymbols = DecimalFormatSymbols(Locale.ROOT)
        private val playbackControlFormatter = DecimalFormat("0.#", decimalFormatSymbols)
    }
}
