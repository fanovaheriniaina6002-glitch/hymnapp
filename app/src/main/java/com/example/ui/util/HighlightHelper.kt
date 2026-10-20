package com.example.ui.util

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ReplacementSpan
import kotlin.math.roundToInt

/**
 * 60 FPS Rounded Corner Background Span for Intelligent Search Highlighting.
 * Soft yellow (#FFF59D) with rounded corners and clean padding.
 */
class RoundedHighlightSpan(
    private val backgroundColor: Int = 0xFFFFF59D.toInt(),
    private val cornerRadius: Float = 10f,
    private val horizontalPadding: Float = 6f
) : ReplacementSpan() {

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return (paint.measureText(text, start, end) + (horizontalPadding * 2)).roundToInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val textWidth = paint.measureText(text, start, end)
        val rect = RectF(
            x,
            top.toFloat() + 2f,
            x + textWidth + (horizontalPadding * 2),
            bottom.toFloat() - 2f
        )
        val oldColor = paint.color
        paint.color = backgroundColor
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        paint.color = oldColor
        canvas.drawText(text, start, end, x + horizontalPadding, y.toFloat(), paint)
    }
}

object HighlightHelper {

    const val COLOR_SOFT_YELLOW = 0xFFFFF59D.toInt()

    fun highlightText(
        fullText: CharSequence,
        query: String?,
        isVisible: Boolean = true
    ): CharSequence {
        if (!isVisible || query.isNullOrBlank()) {
            return fullText
        }
        val q = query.trim()
        if (q.all { it.isDigit() }) {
            return fullText
        }

        val textStr = fullText.toString()
        val textLower = textStr.lowercase()
        val qLower = q.lowercase()

        val firstIdx = textLower.indexOf(qLower)
        if (firstIdx == -1) {
            return fullText
        }

        val spannable = SpannableStringBuilder(fullText)
        var start = 0
        while (start < textLower.length) {
            val idx = textLower.indexOf(qLower, start)
            if (idx == -1) break
            spannable.setSpan(
                RoundedHighlightSpan(
                    backgroundColor = COLOR_SOFT_YELLOW,
                    cornerRadius = 8f,
                    horizontalPadding = 4f
                ),
                idx,
                idx + q.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            start = idx + q.length
        }
        return spannable
    }
}
