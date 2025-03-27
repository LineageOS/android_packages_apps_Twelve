/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import org.lineageos.twelve.R

class AlbumArtDoubleTapImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    // Callbacks
    private var onDoubleLeftTapListener: (() -> Unit)? = null
    private var onDoubleRightTapListener: (() -> Unit)? = null

    // Paints
    private val curvePaint = Paint().apply {
        color = Color.argb(100, 0, 0, 0)
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    enum class Side {
        LEFT, RIGHT
    }

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (e.x < width / 2f) {
                    onDoubleLeftTapListener?.invoke()
                    show(Side.LEFT)
                } else {
                    onDoubleRightTapListener?.invoke()
                    show(Side.RIGHT)
                }
                return true
            }
        })


    init {
        isClickable = true
        isFocusable = true
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent) =
        gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)


    private var side: Side? = null
    private var animationStart = 0L
    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Check if the animation is active
        side?.let { side ->
            val elapsed = System.currentTimeMillis() - animationStart
            if (elapsed < 1000) {
                // Set alpha based on elapsed time
                val alpha = (255 * (1 - elapsed / 1000f)).toInt()
                curvePaint.alpha = alpha
                textPaint.alpha = alpha

                // Clear previous path
                path.reset()

                // Create a path for the bezier curve
                when (side) {
                    Side.LEFT -> {
                        // Move to the top left corner
                        path.moveTo(0f, 0f)

                        // Draw a line up to 3/10 of the width
                        path.lineTo(width * 3f / 10f, 0f)

                        // Draw the quadratic bezier curve.
                        // The control point is at 7/10 of the width and 1/2 of the height
                        path.quadTo(
                            width * 7f / 10f,
                            height.toFloat() / 2f,
                            width * 3f / 10f,
                            height.toFloat(),
                        )

                        // Draw a line to the bottom left corner
                        path.lineTo(0f, height.toFloat())

                        // Close the path
                        path.lineTo(0f, 0f)
                    }

                    Side.RIGHT -> {
                        // Move to the top right corner
                        path.moveTo(width.toFloat(), 0f)

                        // Draw a line up to 7/10 of the width
                        path.lineTo(width * 7f / 10f, 0f)

                        // Draw the quadratic bezier curve.
                        // The control point is at 3/10 of the width and 1/2 of the height
                        path.quadTo(
                            width * 3f / 10f,
                            height.toFloat() / 2f,
                            width * 7f / 10f,
                            height.toFloat(),
                        )

                        // Draw a line to the bottom right corner
                        path.lineTo(width.toFloat(), height.toFloat())

                        // Close the path
                        path.lineTo(width.toFloat(), 0f)
                    }
                }

                // Draw the path
                canvas.drawPath(path, curvePaint)

                // Draw text
                when (side) {
                    Side.LEFT -> canvas.drawText(
                        context.getString(R.string.album_art_double_click_left),
                        width / 4f,
                        height / 2f,
                        textPaint,
                    )

                    Side.RIGHT -> canvas.drawText(
                        context.getString(R.string.album_art_double_click_right),
                        width * 3f / 4f,
                        height / 2f,
                        textPaint,
                    )
                }

                invalidate()
            } else {
                this.side = null
            }
        }
    }

    private fun show(side: Side) {
        this.side = side
        animationStart = System.currentTimeMillis()
        invalidate()
    }

    fun setOnDoubleLeftTapListener(listener: () -> Unit) {
        onDoubleLeftTapListener = listener
    }

    fun setOnDoubleRightTapListener(listener: () -> Unit) {
        onDoubleRightTapListener = listener
    }
}