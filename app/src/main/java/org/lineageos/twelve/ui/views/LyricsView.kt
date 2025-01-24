/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import org.lineageos.twelve.R
import org.lineageos.twelve.models.Lyrics.LyricLine

class LyricsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var lyrics: List<LyricLine>? = null

    // Views
    private val previousTextView by lazy { findViewById<TextView>(R.id.previousTextView) }
    private val currentTextView by lazy { findViewById<TextView>(R.id.currentTextView) }
    private val nextTextView by lazy { findViewById<TextView>(R.id.nextTextView) }

    init {
        LayoutInflater.from(context).inflate(R.layout.lyrics_view, this, true)
    }

    private fun updateLyrics(previous: String, current: String, next: String) {
        previousTextView.text = previous
        currentTextView.text = current
        nextTextView.text = next
    }

    fun syncLyrics(currentPositionMs: Float) {
        val lyrics = lyrics ?: return

        val nextLyricIndex = lyrics.indexOfFirst { lyric ->
            val lyricStartTime = lyric.start
            lyricStartTime > currentPositionMs
        }

        if (nextLyricIndex != -1) {
            val current = if (nextLyricIndex > 0) lyrics[nextLyricIndex - 1].line else ""
            val previous = if (nextLyricIndex > 1) lyrics[nextLyricIndex - 2].line else ""
            val next = lyrics[nextLyricIndex].line
            updateLyrics(previous, current, next)
        }
    }
}
