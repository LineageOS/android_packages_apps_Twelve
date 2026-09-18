/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.os.SystemClock
import android.view.Choreographer
import android.widget.TextView
import com.google.android.material.slider.Slider
import org.lineageos.twelve.ext.updateValueAndRange
import org.lineageos.twelve.utils.TimestampFormatter
import kotlin.math.abs

/**
 * The player only reports position periodically (e.g. once a second), so we have to extrapolate
 * the position every frame via [Choreographer] to keep the slider moving smoothly.
 * When a new report differs slightly from where we extrapolated to the position is adjusted into
 * sync instead of snapping, unless the discrepancy is too large.
 */
class PlaybackProgressSlider(
    private val slider: Slider,
    private val currentTimestampTextView: TextView,
) : Choreographer.FrameCallback {
    private var isPlaying = false
    private var dragState: DragState = DragState.Idle
    private var lastProgress: Progress? = null
    private val frameCallback = Choreographer.FrameCallback(this::doFrame)

    fun update(
        durationMs: Long?,
        currentPositionMs: Long?,
        playbackSpeed: Float,
    ) {
        val nowMs = SystemClock.elapsedRealtime()
        val reportedProgress = Progress(
            durationMs = durationMs ?: 0L,
            currentPositionMs = currentPositionMs ?: 0L,
            playbackSpeed = playbackSpeed,
            updatedAtMs = nowMs,
        )

        val progress = when (dragState) {
            // Trust the user, and that the next report will have the correct position.
            DragState.Pending -> reportedProgress.also { dragState = DragState.Idle }
            else -> smoothlyCorrect(reportedProgress, nowMs)
        }
        lastProgress = progress

        slider.updateValueAndRange(
            value = progress.currentPositionAt(nowMs).toFloat(),
            valueTo = progress.durationMs.toFloat().takeIf { it > 0 } ?: 1f,
            isDragging = dragState == DragState.Dragging,
        )

        if (dragState == DragState.Idle) {
            if (isPlaying) {
                startFrameUpdates(progress)
            } else {
                show(progress.currentPositionMs)
            }
        }
    }

    fun setIsPlaying(isPlaying: Boolean) {
        val nowMs = SystemClock.elapsedRealtime()

        if (this.isPlaying && !isPlaying) {
            lastProgress = lastProgress?.at(nowMs)
        }
        this.isPlaying = isPlaying

        if (isPlaying) {
            lastProgress = lastProgress?.reanchor(nowMs)
            lastProgress?.let {
                startFrameUpdates(it)
            }
        } else {
            stopFrameUpdates()
        }
    }

    fun startDragging() {
        dragState = DragState.Dragging
        stopFrameUpdates()
    }

    fun stopDragging() {
        dragState = DragState.Pending
    }

    fun stop() {
        stopFrameUpdates()
    }

    private fun startFrameUpdates(progress: Progress) {
        stopFrameUpdates()

        show(progress.currentPositionAt(SystemClock.elapsedRealtime()))
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    private fun stopFrameUpdates() {
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    private fun smoothlyCorrect(reportedProgress: Progress, nowMs: Long): Progress {
        val previousProgress = lastProgress ?: return reportedProgress

        if (!isPlaying ||
            dragState != DragState.Idle ||
            previousProgress.durationMs != reportedProgress.durationMs
        ) {
            return reportedProgress
        }

        val displayedPositionMs = previousProgress.currentPositionAt(nowMs)
        val correctionMs = reportedProgress.currentPositionMs - displayedPositionMs

        // Avoid corrections that are too large.
        if (abs(correctionMs) > 2000) {
            return reportedProgress
        }

        return reportedProgress.copy(
            currentPositionMs = displayedPositionMs,
            playbackSpeed = reportedProgress.playbackSpeed + correctionMs.toFloat() / 1000,
            updatedAtMs = nowMs,
        )
    }

    private fun show(positionMs: Long) {
        val position = positionMs.toFloat().coerceIn(slider.valueFrom, slider.valueTo)
        slider.value = position
        currentTimestampTextView.text = TimestampFormatter.formatTimestampMillis(position)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!isPlaying || dragState != DragState.Idle) return

        lastProgress?.let {
            show(it.currentPositionAt(SystemClock.elapsedRealtime()))
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    private sealed interface DragState {
        data object Idle : DragState
        data object Dragging : DragState
        data object Pending : DragState
    }

    private data class Progress(
        val durationMs: Long,
        val currentPositionMs: Long,
        val playbackSpeed: Float,
        val updatedAtMs: Long,
    ) {
        fun currentPositionAt(nowMs: Long): Long =
            (currentPositionMs + (nowMs - updatedAtMs) * playbackSpeed).toLong()
                .coerceIn(0L, durationMs)

        fun at(nowMs: Long) = copy(
            currentPositionMs = currentPositionAt(nowMs),
            updatedAtMs = nowMs,
        )

        fun reanchor(nowMs: Long) = copy(updatedAtMs = nowMs)
    }
}