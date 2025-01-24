package org.lineageos.twelve.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import org.lineageos.twelve.R
import org.lineageos.twelve.models.Lyrics
import org.lineageos.twelve.models.Lyrics.LyricLine

class LyricsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var lyrics: List<LyricLine>? = null

    // Views
    private val previousTextView: TextView
    private val currentTextView: TextView
    private val nextTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.lyrics_view, this, true)

        previousTextView = findViewById(R.id.previousTextView)
        currentTextView = findViewById(R.id.currentTextView)
        nextTextView = findViewById(R.id.nextTextView)
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
            val next = lyrics[nextLyricIndex + 1].line
            updateLyrics(previous, current, next)
        }
    }
}
