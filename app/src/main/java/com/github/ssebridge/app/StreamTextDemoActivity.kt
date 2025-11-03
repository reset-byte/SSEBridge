package com.github.ssebridge.app

import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.github.ssebridge.app.databinding.ActivityStreamTextDemoBinding
import com.github.ssebridge.widget.StreamState
import com.github.ssebridge.widget.StreamTextView

/**
 * StreamTextDemoActivity - Demo activity for StreamTextView
 * 
 * Demonstrates the usage of StreamTextView with streaming text effect,
 * gradient colors, and blinking cursor animation.
 */
class StreamTextDemoActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityStreamTextDemoBinding
    
    companion object {
        private const val DEMO_TEXT_SHORT = "Hello, this is a streaming text demo with gradient effect!"
        private val DEMO_TEXT_LONG = """
            This is a comprehensive demonstration of the StreamTextView component. 
            
            Key Features:
            1. Character-by-character streaming display
            2. Beautiful gradient color effects
            3. Animated blinking cursor
            4. Three states: IDLE, STREAMING, COMPLETED
            5. Fully customizable colors and timing
            
            The component is perfect for AI chat interfaces, loading messages, 
            and any scenario where you need elegant progressive text display.
        """.trimIndent()
        
        private val DEMO_TEXT_SSE = """
            根据server返回的数据进行流式展示：
            
            初始等待 → 需要展示光标并进行光标闪烁
            展示中 → 要求逐个展示文案且光标闪烁
            展示结束 → 需要展示完整文案以及隐藏光标
            
            这是一个完整的SSE流式文本展示组件！
        """.trimIndent()
        
        private const val DEFAULT_DELAY_MS = 50L
        private const val MIN_DELAY_MS = 10L
        private const val MAX_DELAY_MS = 200L
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStreamTextDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupStreamTextView()
        setupControls()
        setupSeekBar()
    }
    
    /**
     * Setup StreamTextView with callbacks
     */
    private fun setupStreamTextView() {
        binding.streamTextView.apply {
            setGradientColors(
                Color.parseColor("#FF6B6B"),
                Color.parseColor("#4ECDC4")
            )
            setCursorColor(Color.parseColor("#4E6EF2"))
            setOnStreamStartListener {
                updateStatus("流式展示已开始...")
                updateControlButtons(isStreaming = true)
            }
            setOnStreamProgressListener { progress ->
                updateProgress(progress)
            }
            setOnStreamCompleteListener {
                updateStatus("流式展示已完成！")
                updateControlButtons(isStreaming = false)
            }
        }
    }
    
    /**
     * Setup control buttons
     */
    private fun setupControls() {
        binding.startShortButton.setOnClickListener {
            startStreamingText(DEMO_TEXT_SHORT)
        }
        binding.startLongButton.setOnClickListener {
            startStreamingText(DEMO_TEXT_LONG)
        }
        binding.startSseButton.setOnClickListener {
            startStreamingText(DEMO_TEXT_SSE)
        }
        binding.stopButton.setOnClickListener {
            executeStopStreaming()
        }
        binding.resetButton.setOnClickListener {
            executeReset()
        }
        binding.colorPreset1Button.setOnClickListener {
            applyColorPreset1()
        }
        binding.colorPreset2Button.setOnClickListener {
            applyColorPreset2()
        }
        binding.colorPreset3Button.setOnClickListener {
            applyColorPreset3()
        }
    }
    
    /**
     * Setup speed control SeekBar
     */
    private fun setupSeekBar() {
        binding.speedSeekBar.max = (MAX_DELAY_MS - MIN_DELAY_MS).toInt()
        binding.speedSeekBar.progress = (DEFAULT_DELAY_MS - MIN_DELAY_MS).toInt()
        updateSpeedLabel(DEFAULT_DELAY_MS)
        binding.speedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val delayMs: Long = MIN_DELAY_MS + progress
                updateSpeedLabel(delayMs)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    /**
     * Start streaming text
     */
    private fun startStreamingText(text: String) {
        val delayMs: Long = calculateCurrentDelay()
        binding.streamTextView.setStreamText(text, delayMs)
        binding.streamTextView.startStreaming()
        updateStatus("流式展示进行中...")
        updateProgress(0f)
    }
    
    /**
     * Stop streaming
     */
    private fun executeStopStreaming() {
        binding.streamTextView.stopStreaming()
        updateStatus("流式展示已停止")
        updateControlButtons(isStreaming = false)
    }
    
    /**
     * Reset StreamTextView
     */
    private fun executeReset() {
        binding.streamTextView.reset()
        updateStatus("已重置")
        updateProgress(0f)
        updateControlButtons(isStreaming = false)
    }
    
    /**
     * Apply color preset 1 - Warm colors
     */
    private fun applyColorPreset1() {
        binding.streamTextView.setGradientColors(
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#FFA726")
        )
        binding.streamTextView.setCursorColor(Color.parseColor("#F44336"))
        updateStatus("已应用暖色调配色")
    }
    
    /**
     * Apply color preset 2 - Cool colors
     */
    private fun applyColorPreset2() {
        binding.streamTextView.setGradientColors(
            Color.parseColor("#4ECDC4"),
            Color.parseColor("#556EE6")
        )
        binding.streamTextView.setCursorColor(Color.parseColor("#2196F3"))
        updateStatus("已应用冷色调配色")
    }
    
    /**
     * Apply color preset 3 - Purple gradient
     */
    private fun applyColorPreset3() {
        binding.streamTextView.setGradientColors(
            Color.parseColor("#B06AB3"),
            Color.parseColor("#4568DC")
        )
        binding.streamTextView.setCursorColor(Color.parseColor("#9C27B0"))
        updateStatus("已应用紫色渐变配色")
    }
    
    /**
     * Update status text
     */
    private fun updateStatus(status: String) {
        binding.statusText.text = status
    }
    
    /**
     * Update progress
     */
    private fun updateProgress(progress: Float) {
        val percentage: Int = (progress * 100).toInt()
        binding.progressText.text = "进度: $percentage%"
        binding.progressBar.progress = percentage
    }
    
    /**
     * Update control buttons state
     */
    private fun updateControlButtons(isStreaming: Boolean) {
        binding.startShortButton.isEnabled = !isStreaming
        binding.startLongButton.isEnabled = !isStreaming
        binding.startSseButton.isEnabled = !isStreaming
        binding.stopButton.isEnabled = isStreaming
        binding.resetButton.isEnabled = !isStreaming
    }
    
    /**
     * Update speed label
     */
    private fun updateSpeedLabel(delayMs: Long) {
        binding.speedLabel.text = "速度: ${delayMs}ms/字符"
    }
    
    /**
     * Calculate current delay from SeekBar
     */
    private fun calculateCurrentDelay(): Long {
        return MIN_DELAY_MS + binding.speedSeekBar.progress
    }
}

