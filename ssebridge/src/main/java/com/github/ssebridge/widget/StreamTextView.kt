package com.github.ssebridge.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView

/**
 * StreamTextView - Custom TextView for streaming text display with gradient effect
 * 
 * This view provides a streaming text display effect with the following features:
 * - Character-by-character text display
 * - Gradient color effect on displayed text
 * - Blinking cursor at the end of text
 * - Three states: IDLE, STREAMING, COMPLETED
 * 
 * Usage:
 * ```
 * val streamTextView = StreamTextView(context)
 * streamTextView.setStreamText("Your text here", delayMillis = 50)
 * ```
 */
class StreamTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    
    private var fullText: String = ""
    private var currentIndex: Int = 0
    private var currentState: StreamState = StreamState.IDLE
    private var streamDelayMillis: Long = 50L
    
    // Colors
    @ColorInt
    private var gradientStartColor: Int = Color.parseColor("#FF6B6B")
    
    @ColorInt
    private var gradientEndColor: Int = Color.parseColor("#4ECDC4")
    
    @ColorInt
    private var cursorColor: Int = Color.parseColor("#4E6EF2")
    
    // Handlers and Animators
    private val streamHandler: Handler = Handler(Looper.getMainLooper())
    private var cursorAnimator: ValueAnimator? = null
    private var cursorSpan: CursorSpan? = null
    
    // Callbacks
    private var onStreamStartCallback: (() -> Unit)? = null
    private var onStreamProgressCallback: ((progress: Float) -> Unit)? = null
    private var onStreamCompleteCallback: (() -> Unit)? = null
    
    private val streamRunnable: Runnable = object : Runnable {
        override fun run() {
            if (currentIndex < fullText.length) {
                currentIndex++
                updateDisplayText()
                streamHandler.postDelayed(this, streamDelayMillis)
                val progress: Float = currentIndex.toFloat() / fullText.length
                onStreamProgressCallback?.invoke(progress)
            } else {
                finishStreaming()
            }
        }
    }
    
    init {
        initializeCursor()
    }
    
    /**
     * Initialize cursor with blinking animation
     */
    private fun initializeCursor() {
        cursorSpan = CursorSpan(context, cursorColor)
        cursorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 530L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                cursorSpan?.alpha = animation.animatedValue as Float
                invalidate()
            }
        }
    }
    
    /**
     * Set streaming text and start display
     * 
     * @param text The text to display in streaming mode
     * @param delayMillis Delay between each character display (default 50ms)
     */
    fun setStreamText(text: String, delayMillis: Long = 50L) {
        stopStreaming()
        fullText = text
        currentIndex = 0
        streamDelayMillis = delayMillis
        currentState = StreamState.IDLE
        startIdleState()
    }
    
    /**
     * Start streaming display
     */
    fun startStreaming() {
        if (currentState == StreamState.IDLE && fullText.isNotEmpty()) {
            currentState = StreamState.STREAMING
            currentIndex = 0
            startCursorBlink()
            streamHandler.post(streamRunnable)
            onStreamStartCallback?.invoke()
        }
    }
    
    /**
     * Stop streaming display
     */
    fun stopStreaming() {
        streamHandler.removeCallbacks(streamRunnable)
        stopCursorBlink()
        currentState = StreamState.COMPLETED
    }
    
    /**
     * Reset to initial state
     */
    fun reset() {
        stopStreaming()
        fullText = ""
        currentIndex = 0
        text = ""
        currentState = StreamState.IDLE
    }
    
    /**
     * Check if currently streaming
     */
    fun isStreaming(): Boolean = currentState == StreamState.STREAMING
    
    /**
     * Get current stream state
     */
    fun getStreamState(): StreamState = currentState
    
    /**
     * Set gradient colors
     */
    fun setGradientColors(@ColorInt startColor: Int, @ColorInt endColor: Int) {
        gradientStartColor = startColor
        gradientEndColor = endColor
        if (currentState == StreamState.STREAMING) {
            updateDisplayText()
        }
    }
    
    /**
     * Set cursor color
     */
    fun setCursorColor(@ColorInt color: Int) {
        cursorColor = color
        cursorSpan = CursorSpan(context, cursorColor)
    }
    
    /**
     * Set stream start callback
     */
    fun setOnStreamStartListener(callback: () -> Unit) {
        onStreamStartCallback = callback
    }
    
    /**
     * Set stream progress callback
     */
    fun setOnStreamProgressListener(callback: (progress: Float) -> Unit) {
        onStreamProgressCallback = callback
    }
    
    /**
     * Set stream complete callback
     */
    fun setOnStreamCompleteListener(callback: () -> Unit) {
        onStreamCompleteCallback = callback
    }
    
    /**
     * Start idle state with blinking cursor only
     */
    private fun startIdleState() {
        currentState = StreamState.IDLE
        val builder = SpannableStringBuilder(" ")
        cursorSpan?.let {
            builder.setSpan(it, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        text = builder
        startCursorBlink()
    }
    
    /**
     * Update displayed text with gradient and cursor
     */
    private fun updateDisplayText() {
        if (currentIndex == 0) return
        val displayText: String = fullText.substring(0, currentIndex)
        val builder = SpannableStringBuilder(displayText + " ")
        if (currentIndex > 0) {
            val gradientSpan = LinearGradientSpan(
                containingText = displayText,
                gradientStart = 0,
                gradientEnd = currentIndex,
                startColorInt = gradientStartColor,
                endColorInt = gradientEndColor
            )
            builder.setSpan(gradientSpan, 0, currentIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        cursorSpan?.let {
            builder.setSpan(it, currentIndex, currentIndex + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        text = builder
    }
    
    /**
     * Finish streaming and hide cursor
     */
    private fun finishStreaming() {
        currentState = StreamState.COMPLETED
        stopCursorBlink()
        val builder = SpannableStringBuilder(fullText)
        val gradientSpan = LinearGradientSpan(
            containingText = fullText,
            gradientStart = 0,
            gradientEnd = fullText.length,
            startColorInt = gradientStartColor,
            endColorInt = gradientEndColor
        )
        builder.setSpan(gradientSpan, 0, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text = builder
        onStreamCompleteCallback?.invoke()
    }
    
    /**
     * Start cursor blinking animation
     */
    private fun startCursorBlink() {
        cursorAnimator?.start()
    }
    
    /**
     * Stop cursor blinking animation
     */
    private fun stopCursorBlink() {
        cursorAnimator?.cancel()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopStreaming()
        cursorAnimator?.cancel()
    }
}

