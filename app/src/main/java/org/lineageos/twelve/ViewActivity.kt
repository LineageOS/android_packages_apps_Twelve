/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.util.Consumer
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil3.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.lineageos.twelve.datasources.MediaType
import org.lineageos.twelve.models.RequestStatus
import org.lineageos.twelve.utils.PermissionsChecker
import org.lineageos.twelve.utils.PermissionsUtils
import org.lineageos.twelve.utils.TimestampFormatter
import org.lineageos.twelve.viewmodels.IntentsViewModel
import org.lineageos.twelve.viewmodels.LocalPlayerViewModel
import kotlin.math.roundToLong

/**
 * An activity used to handle view intents.
 */
class ViewActivity : AppCompatActivity(R.layout.activity_view) {
    // View models
    private val intentsViewModel by viewModels<IntentsViewModel>()
    private val localPlayerViewModel by viewModels<LocalPlayerViewModel>()

    // Views
    private val albumTitleTextView by lazy { findViewById<TextView>(R.id.albumTitleTextView) }
    private val artistNameTextView by lazy { findViewById<TextView>(R.id.artistNameTextView) }
    private val audioTitleTextView by lazy { findViewById<TextView>(R.id.audioTitleTextView) }
    private val currentTimestampTextView by lazy { findViewById<TextView>(R.id.currentTimestampTextView) }
    private val dummyThumbnailImageView by lazy { findViewById<ImageView>(R.id.dummyThumbnailImageView) }
    private val durationTimestampTextView by lazy { findViewById<TextView>(R.id.durationTimestampTextView) }
    private val playPauseMaterialButton by lazy { findViewById<MaterialButton>(R.id.playPauseMaterialButton) }
    private val progressSlider by lazy { findViewById<Slider>(R.id.progressSlider) }
    private val thumbnailImageView by lazy { findViewById<ImageView>(R.id.thumbnailImageView) }

    // Progress slider state
    private var isProgressSliderDragging = false
    private var animator: ValueAnimator? = null

    // Permissions
    private val permissionsChecker = PermissionsChecker(
        this, PermissionsUtils.mainPermissions
    )

    // Intents
    private val intentListener = Consumer<Intent> { intentsViewModel.onIntent(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Audio information
        audioTitleTextView.isSelected = true
        artistNameTextView.isSelected = true
        albumTitleTextView.isSelected = true

        // Media controls
        progressSlider.setLabelFormatter {
            TimestampFormatter.formatTimestampMillis(it)
        }
        progressSlider.addOnSliderTouchListener(
            object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    isProgressSliderDragging = true
                    animator?.cancel()
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    isProgressSliderDragging = false
                    localPlayerViewModel.seekToPosition(slider.value.roundToLong())
                }
            }
        )

        playPauseMaterialButton.setOnClickListener {
            localPlayerViewModel.togglePlayPause()
        }

        intentsViewModel.onIntent(intent)
        addOnNewIntentListener(intentListener)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsChecker.withPermissionsGranted {
                    loadData()
                }
            }
        }
    }

    private suspend fun loadData() {
        coroutineScope {
            launch {
                localPlayerViewModel.mediaMetadata.collectLatest { mediaMetadata ->
                    mediaMetadata.albumTitle?.also {
                        if (albumTitleTextView.text != it) {
                            albumTitleTextView.text = it
                        }
                        albumTitleTextView.isVisible = true
                    } ?: run {
                        albumTitleTextView.isVisible = false
                    }

                    mediaMetadata.artist?.also {
                        if (artistNameTextView.text != it) {
                            artistNameTextView.text = it
                        }
                        artistNameTextView.isVisible = true
                    } ?: run {
                        artistNameTextView.isVisible = false
                    }

                    mediaMetadata.title?.also {
                        if (audioTitleTextView.text != it) {
                            audioTitleTextView.text = it
                        }
                        audioTitleTextView.isVisible = true
                    } ?: run {
                        audioTitleTextView.isVisible = false
                    }
                }
            }

            launch {
                localPlayerViewModel.isPlaying.collectLatest { isPlaying ->
                    playPauseMaterialButton.setIconResource(
                        when (isPlaying) {
                            true -> R.drawable.ic_pause
                            false -> R.drawable.ic_play_arrow
                        }
                    )
                }
            }

            launch {
                localPlayerViewModel.mediaArtwork.collectLatest {
                    when (it) {
                        is RequestStatus.Loading -> {
                            // Do nothing
                        }

                        is RequestStatus.Success -> {
                            it.data?.bitmap?.let { bitmap ->
                                thumbnailImageView.load(bitmap)
                                thumbnailImageView.isVisible = true
                                dummyThumbnailImageView.isVisible = false
                            } ?: it.data?.uri?.let { uri ->
                                thumbnailImageView.load(uri)
                                thumbnailImageView.isVisible = true
                                dummyThumbnailImageView.isVisible = false
                            } ?: run {
                                thumbnailImageView.isVisible = false
                                dummyThumbnailImageView.isVisible = true
                            }
                        }

                        is RequestStatus.Error -> {
                            Log.e(LOG_TAG, "Failed to load artwork")
                            dummyThumbnailImageView.isVisible = true
                            thumbnailImageView.isVisible = false
                        }
                    }
                }
            }

            launch {
                // Restart animation based on this value being changed
                var oldValue = 0f

                localPlayerViewModel.durationCurrentPositionMs.collectLatest { durationCurrentPositionMs ->
                    val (durationMs, currentPositionMs, playbackSpeed) =
                        durationCurrentPositionMs.let {
                            Triple(
                                it.first ?: 0L,
                                it.second ?: 0L,
                                it.third
                            )
                        }

                    // We want to lose ms precision with the slider
                    val durationSecs = durationMs / 1000
                    val currentPositionSecs = currentPositionMs / 1000

                    val newValueTo = (durationSecs * 1000).toFloat().takeIf { it > 0 } ?: 1f
                    val newValue = (currentPositionSecs * 1000).toFloat()

                    val valueToChanged = progressSlider.valueTo != newValueTo
                    val valueChanged = oldValue != newValue

                    // Only +1s should be animated
                    val shouldBeAnimated = (newValue - oldValue) == 1000f

                    val newAnimator = ValueAnimator.ofFloat(
                        progressSlider.value, newValue
                    ).apply {
                        interpolator = LinearInterpolator()
                        duration = 1000 / playbackSpeed.roundToLong()
                        doOnStart {
                            // Update valueTo at the start of the animation
                            if (progressSlider.valueTo != newValueTo) {
                                progressSlider.valueTo = newValueTo
                            }
                        }
                        addUpdateListener {
                            progressSlider.value = (it.animatedValue as Float)
                        }
                    }

                    oldValue = newValue

                    /**
                     * Update only if:
                     * - The value changed and the user isn't dragging the slider
                     * - valueTo changed
                     */
                    if ((!isProgressSliderDragging && valueChanged) || valueToChanged) {
                        val afterOldAnimatorEnded = {
                            if (shouldBeAnimated) {
                                animator = newAnimator
                                newAnimator.start()
                            } else {
                                animator = null
                                // Update both valueTo and value
                                progressSlider.valueTo = newValueTo
                                progressSlider.value = newValue
                            }
                        }

                        animator?.also { oldAnimator ->
                            // Start the new animation right after old one finishes
                            oldAnimator.doOnEnd {
                                afterOldAnimatorEnded()
                            }

                            if (oldAnimator.isRunning) {
                                oldAnimator.cancel()
                            } else {
                                oldAnimator.end()
                            }
                        } ?: run {
                            // This is the first animation
                            afterOldAnimatorEnded()
                        }
                    }

                    currentTimestampTextView.text = TimestampFormatter.formatTimestampMillis(
                        currentPositionMs
                    )
                    durationTimestampTextView.text = TimestampFormatter.formatTimestampMillis(
                        durationMs
                    )
                }
            }

            launch {
                intentsViewModel.parsedIntent.collectLatest { parsedIntent ->
                    parsedIntent?.handle {
                        if (it.action != IntentsViewModel.ParsedIntent.Action.VIEW) {
                            Log.e(LOG_TAG, "Cannot handle action ${it.action}")
                            finish()
                            return@handle
                        }

                        if (it.contents.isEmpty()) {
                            Log.e(LOG_TAG, "No content to play")
                            finish()
                            return@handle
                        }

                        val contentType = it.contents.first().type
                        if (contentType != MediaType.AUDIO) {
                            Log.e(LOG_TAG, "Cannot handle content type $contentType")
                            finish()
                            return@handle
                        }

                        if (it.contents.any { content -> content.type != contentType }) {
                            Log.e(LOG_TAG, "All contents must have the same type")
                            finish()
                            return@handle
                        }

                        localPlayerViewModel.setMediaUris(
                            it.contents.map { content -> content.uri }
                        )
                    }
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = ViewActivity::class.simpleName!!
    }
}
