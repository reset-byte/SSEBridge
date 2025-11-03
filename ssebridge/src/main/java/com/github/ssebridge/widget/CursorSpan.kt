package com.github.ssebridge.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi

/**
 * CursorSpan - Custom span for cursor rendering
 * 
 * This span replaces text with a cursor-like visual effect.
 * It extends ReplacementSpan to provide custom size calculation and drawing.
 * 
 * @param context Android context for resource access
 * @param cursorColor The color of the cursor (ARGB format)
 */
class CursorSpan(
    private val context: Context,
    @ColorInt private val cursorColor: Int
) : ReplacementSpan() {
    
    var alpha: Float = 1f
    var radiusX: Float = 4f
    var radiusY: Float = 4f
    var cursorWidth: Int = 8
    
    private val cursorPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = cursorColor
    }
    
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        fm?.let {
            val fontMetricsInt: Paint.FontMetricsInt = paint.fontMetricsInt
            it.ascent = fontMetricsInt.ascent
            it.descent = fontMetricsInt.descent
            it.top = fontMetricsInt.top
            it.bottom = fontMetricsInt.bottom
        }
        return cursorWidth
    }
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        cursorPaint.alpha = (alpha * 255).toInt().coerceAtMost(255)
        canvas.drawRoundRect(
            x,
            top.toFloat(),
            x + cursorWidth,
            bottom.toFloat(),
            radiusX,
            radiusY,
            cursorPaint
        )
    }
}

