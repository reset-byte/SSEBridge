package com.github.ssebridge.widget

import android.graphics.LinearGradient
import android.graphics.Shader
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import androidx.annotation.ColorInt

/**
 * LinearGradientSpan - Custom span for text gradient effect
 * 
 * This span applies a linear gradient color effect to text characters.
 * It extends CharacterStyle and implements UpdateAppearance to customize text rendering.
 * 
 * @param containingText The complete text that contains the gradient portion
 * @param gradientStart The start position of the gradient effect
 * @param gradientEnd The end position of the gradient effect
 * @param startColorInt The starting color of the gradient (ARGB format)
 * @param endColorInt The ending color of the gradient (ARGB format)
 */
class LinearGradientSpan(
    private val containingText: String,
    private val gradientStart: Int,
    private val gradientEnd: Int,
    @ColorInt private val startColorInt: Int,
    @ColorInt private val endColorInt: Int
) : CharacterStyle(), UpdateAppearance {
    
    override fun updateDrawState(tp: TextPaint?) {
        tp ?: return
        val leadingWidth: Float = tp.measureText(containingText, 0, gradientStart)
        val gradientWidth: Float = tp.measureText(containingText, gradientStart, gradientEnd)
        val lineGradient = LinearGradient(
            leadingWidth,
            0f,
            leadingWidth + gradientWidth,
            0f,
            intArrayOf(startColorInt, endColorInt),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        tp.shader = lineGradient
    }
}

