package com.github.ssebridge.app

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.ssebridge.SSEBridge
import com.github.ssebridge.app.databinding.ActivityMainBinding
import com.github.ssebridge.data.SSEConfig
import com.github.ssebridge.data.SSEEvent
import com.github.ssebridge.data.SSEState
import com.github.ssebridge.listener.SSEEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * MainActivity - SSEBridge SDK测试入口
 *
 * 提供完整的SSE连接测试功能，包括：
 * - GET/POST请求测试
 * - 连接状态监控
 * - 事件日志显示
 * - 自定义配置
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions"
        private const val DEEPSEEK_API_KEY = ""
        private const val DEFAULT_REQUEST_BODY = """{
  "model": "deepseek-chat",
  "messages": [
    {"role": "system", "content": "You are a helpful assistant."},
    {"role": "user", "content": "请介绍一下你自己"}
  ],
  "stream": true
}"""
    }

    private var sseBridge: SSEBridge? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: EventLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        setupListeners()
        initializeSSEBridge()
        setupDefaultValues()
    }

    /**
     * Setup RecyclerView
     */
    private fun setupRecyclerView() {
        adapter = EventLogAdapter()
        binding.logsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.logsRecyclerView.adapter = adapter
    }

    /**
     * Setup button listeners
     */
    private fun setupListeners() {
        binding.getConnectButton.setOnClickListener {
            executeGetConnection()
        }
        binding.postConnectButton.setOnClickListener {
            executePostConnection()
        }
        binding.disconnectButton.setOnClickListener {
            executeDisconnect()
        }
        binding.clearLogsButton.setOnClickListener {
            executeClearLogs()
        }
    }

    /**
     * Setup default values for DeepSeek API testing
     */
    private fun setupDefaultValues() {
        binding.urlEditText.setText(DEEPSEEK_API_URL)
        binding.bodyEditText.setText(DEFAULT_REQUEST_BODY)
    }

    /**
     * Initialize SSEBridge
     */
    private fun initializeSSEBridge() {
        val config = buildTestConfig()
        val listener = createTestListener()
        sseBridge = SSEBridge.Builder()
            .setConfig(config)
            .setLifecycle(lifecycle)
            .setEventListener(listener)
            .build()
    }

    /**
     * Execute GET connection
     */
    private fun executeGetConnection() {
        val url = binding.urlEditText.text.toString()
        if (url.isNotBlank()) {
            sseBridge?.connectGet(url, emptyMap())
        }
    }

    /**
     * Execute POST connection
     */
    private fun executePostConnection() {
        val url = binding.urlEditText.text.toString()
        val body = binding.bodyEditText.text.toString()
        if (url.isNotBlank()) {
            android.util.Log.d("MainActivity", "=== POST Connection Start ===")
            android.util.Log.d("MainActivity", "URL: $url")
            android.util.Log.d("MainActivity", "Body: $body")
            val headers = buildHeaders()
            android.util.Log.d("MainActivity", "Headers: $headers")
            android.util.Log.d("MainActivity", "============================")
            sseBridge?.connectPost(url, body, headers)
        } else {
            addLog("错误", "URL 不能为空", Color.parseColor("#F44336"))
        }
    }

    /**
     * Build headers with authorization
     */
    private fun buildHeaders(): Map<String, String> {
        return mapOf(
            "Authorization" to "Bearer $DEEPSEEK_API_KEY",
            "Content-Type" to "application/json"
        )
    }

    /**
     * Execute disconnect
     */
    private fun executeDisconnect() {
        sseBridge?.disconnect()
    }

    /**
     * Execute clear logs
     */
    private fun executeClearLogs() {
        adapter.clearLogs()
        updateLogsCount()
    }

    /**
     * Update connection state UI
     */
    private fun updateConnectionState(state: SSEState) {
        runOnUiThread {
            binding.statusText.text = getStateText(state)
            binding.statusText.setTextColor(getStateColor(state))
            binding.statusCard.setCardBackgroundColor(getStateColor(state) and 0x1AFFFFFF)
            val isConnecting = state == SSEState.CONNECTING || state == SSEState.CONNECTED
            binding.getConnectButton.isEnabled = !isConnecting
            binding.postConnectButton.isEnabled = !isConnecting
            binding.disconnectButton.isEnabled = isConnecting
        }
    }

    /**
     * Add log entry
     */
    private fun addLog(type: String, message: String, color: Int) {
        runOnUiThread {
            val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            adapter.addLog(EventLog(type, message, timestamp, color))
            updateLogsCount()
            binding.logsRecyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    /**
     * Update logs count
     */
    private fun updateLogsCount() {
        binding.logsLabel.text = "事件日志 (${adapter.getLogCount()})"
    }

    /**
     * Build test configuration
     */
    private fun buildTestConfig(): SSEConfig {
        return SSEConfig.Builder()
            .setConnectTimeout(30)
            .setReadTimeout(5)
            .setWriteTimeout(30)
            .setTimeUnit(TimeUnit.SECONDS)
            .setEnableLogging(true)
            .build()
    }

    /**
     * Create test event listener
     */
    private fun createTestListener(): SSEEventListener {
        return object : SSEEventListener {
            override fun onStateChanged(state: SSEState) {
                updateConnectionState(state)
                val stateText = getStateText(state)
                android.util.Log.d("MainActivity", "State Changed: $stateText")
                addLog("状态变更", "新状态: $stateText", Color.parseColor("#2196F3"))
            }

            override fun onConnected() {
                android.util.Log.d("MainActivity", "Connected to SSE server")
                addLog("已连接", "成功连接到SSE服务器", Color.parseColor("#4CAF50"))
            }

            override fun onEvent(event: SSEEvent) {
                val message = buildString {
                    event.id?.let { append("ID: $it\n") }
                    event.type?.let { append("类型: $it\n") }
                    append("数据: ${event.data}")
                }
                android.util.Log.d("MainActivity", "Received Event: ${event.data}")
                addLog("收到事件", message, Color.parseColor("#4CAF50"))
            }

            override fun onClosed() {
                android.util.Log.d("MainActivity", "Connection Closed")
                addLog("已关闭", "SSE连接已正常关闭", Color.parseColor("#9E9E9E"))
            }

            override fun onFailure(throwable: Throwable?) {
                val message = throwable?.message ?: "未知错误"
                android.util.Log.e("MainActivity", "Connection Failed", throwable)
                val detailedMessage = buildString {
                    append("错误: $message\n")
                    throwable?.cause?.let { append("原因: ${it.message}\n") }
                    append("\n请查看诊断指南: CONNECTION_TROUBLESHOOTING.md")
                }
                addLog("失败", detailedMessage, Color.parseColor("#F44336"))
            }

            override fun onCancelled() {
                android.util.Log.d("MainActivity", "Connection Cancelled")
                addLog("已取消", "SSE连接已被取消", Color.parseColor("#FF9800"))
            }
        }
    }

    /**
     * Get state color
     */
    private fun getStateColor(state: SSEState): Int {
        return when (state) {
            SSEState.IDLE -> Color.parseColor("#9E9E9E")
            SSEState.CONNECTING -> Color.parseColor("#2196F3")
            SSEState.CONNECTED -> Color.parseColor("#4CAF50")
            SSEState.CLOSED -> Color.parseColor("#9E9E9E")
            SSEState.FAILED -> Color.parseColor("#F44336")
            SSEState.CANCELLED -> Color.parseColor("#FF9800")
        }
    }

    /**
     * Get state text
     */
    private fun getStateText(state: SSEState): String {
        return when (state) {
            SSEState.IDLE -> "空闲"
            SSEState.CONNECTING -> "连接中"
            SSEState.CONNECTED -> "已连接"
            SSEState.CLOSED -> "已关闭"
            SSEState.FAILED -> "失败"
            SSEState.CANCELLED -> "已取消"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sseBridge?.disconnect()
        sseBridge = null
    }
}
