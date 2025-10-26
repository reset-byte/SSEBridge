package com.github.ssebridge.example

import androidx.lifecycle.Lifecycle
import com.github.ssebridge.SSEBridge
import com.github.ssebridge.data.SSEConfig
import com.github.ssebridge.data.SSEEvent
import com.github.ssebridge.data.SSEState
import com.github.ssebridge.listener.SSEEventListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Agent PPT Bridge Adapter
 *
 * This is an example adapter showing how to use SSEBridge to replace
 * the original AgentPptHandler implementation.
 *
 * This adapter encapsulates business logic for AI agent presentation
 * processing while using the generic SSEBridge underneath.
 *
 * @property lifecycle Lifecycle for automatic connection management
 * @property callback Business-specific callback listener
 */
class AgentPptBridgeAdapter(
    private val lifecycle: Lifecycle? = null,
    private val callback: AgentPptCallback? = null
) {

    companion object {
        private const val CONNECTION_URL = "https://api.popai.pro/api/v1/chat/send"
        private const val CONNECTION_CONTINUE_URL = "https://api.popai.pro/api/v1/chat/send-continue"
    }

    /** SSE Bridge instance */
    private val bridge: SSEBridge

    /** Request body JSON */
    private var requestBody: JSONObject? = null

    init {
        val config = SSEConfig.Builder()
            .setConnectTimeout(1)
            .setReadTimeout(2)
            .setWriteTimeout(1)
            .setTimeUnit(TimeUnit.MINUTES)
            .setEnableLogging(true)
            .build()

        bridge = SSEBridge.Builder()
            .setConfig(config)
            .apply { lifecycle?.let { setLifecycle(it) } }
            .setEventListener(createEventListener())
            .build()
    }

    /**
     * Create SSE event listener
     */
    private fun createEventListener(): SSEEventListener {
        return object : SSEEventListener {
            override fun onStateChanged(state: SSEState) {
                when (state) {
                    SSEState.CONNECTED -> callback?.onConnected()
                    SSEState.CLOSED -> callback?.onClosed()
                    SSEState.CANCELLED -> callback?.onCancelled()
                    SSEState.FAILED -> {}
                    else -> {}
                }
            }

            override fun onEvent(event: SSEEvent) {
                processEvent(event)
            }

            override fun onFailure(throwable: Throwable?) {
                callback?.onFailure(throwable)
            }
        }
    }

    /**
     * Process SSE event
     */
    private fun processEvent(event: SSEEvent) {
        try {
            when {
                event.data.contains("channelid", ignoreCase = true) -> {
                    handleChannelMessageList(event.data)
                }
                event.data.contains("chunkid", ignoreCase = true) -> {
                    handleMessageChunk(event.data)
                }
            }
        } catch (e: Exception) {
            callback?.onParseError(e)
        }
    }

    /**
     * Handle channel message list
     */
    private fun handleChannelMessageList(data: String) {
        val jsonArray = JSONArray(data)
        val messages = mutableListOf<MessageData>()
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            val message = MessageData(
                messageId = json.optString("messageId"),
                content = json.optString("content"),
                role = json.optString("role"),
                deleted = json.optBoolean("deleted", false)
            )
            if (!message.deleted) {
                messages.add(message)
            }
        }
        callback?.onMessageListReceived(messages)
    }

    /**
     * Handle message chunk
     */
    private fun handleMessageChunk(data: String) {
        val jsonArray = JSONArray(data)
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            val chunk = MessageChunkData(
                chunkId = json.optString("chunkid"),
                messageId = json.optString("messageId"),
                content = json.optString("content"),
                error = json.optBoolean("error", false),
                last = json.optBoolean("last", false),
                errorMessage = json.optString("errMsg")
            )
            callback?.onMessageChunkReceived(chunk)
        }
    }

    /**
     * Create agent presentation request
     *
     * @param channelId Channel ID
     * @param message User message
     * @param messageId Message ID (optional)
     * @param imageUrls Image URLs (optional)
     * @param fileUrls File URLs (optional)
     */
    fun createAgentPresentation(
        channelId: String,
        message: String = "",
        messageId: String? = null,
        imageUrls: List<String>? = null,
        fileUrls: List<String>? = null
    ) {
        requestBody = buildRequestBody(
            channelId = channelId,
            message = message,
            messageId = messageId,
            imageUrls = imageUrls,
            fileUrls = fileUrls
        )
    }

    /**
     * Build request body JSON
     */
    private fun buildRequestBody(
        channelId: String,
        message: String,
        messageId: String?,
        imageUrls: List<String>?,
        fileUrls: List<String>?
    ): JSONObject {
        return JSONObject().apply {
            put("isGetJson", true)
            put("language", "en")  // Can be made configurable
            put("message", message)
            put("model", "Claude-4-sonnet")
            put("messageId", messageId)
            put("channelId", channelId)
            put("isNewChat", false)
            put("isGeneratePpt", false)
            put("isSlidesChat", false)
            put("isImprove", false)

            if (!imageUrls.isNullOrEmpty()) {
                val jsonArray = JSONArray()
                imageUrls.forEach { jsonArray.put(it) }
                put("imageUrls", jsonArray)
            }

            if (!fileUrls.isNullOrEmpty()) {
                val jsonArray = JSONArray()
                fileUrls.forEach { jsonArray.put(it) }
                put("fileUrls", jsonArray)
            }
        }
    }

    /**
     * Connect to SSE server
     *
     * @param isFinish Whether this is a finish request
     */
    fun connect(isFinish: Boolean = true) {
        val url = if (isFinish) CONNECTION_URL else CONNECTION_CONTINUE_URL
        val body = requestBody?.toString() ?: "{}"
        bridge.connectPost(url, body)
    }

    /**
     * Disconnect from SSE server
     */
    fun disconnect() {
        bridge.disconnect()
    }

    /**
     * Check if currently connecting
     */
    fun isConnecting(): Boolean {
        return bridge.isConnecting()
    }

    /**
     * Get current connection state
     */
    fun getState(): SSEState {
        return bridge.getState()
    }
}

/**
 * Business-specific callback interface
 */
interface AgentPptCallback {
    fun onConnected()
    fun onMessageListReceived(messages: List<MessageData>)
    fun onMessageChunkReceived(chunk: MessageChunkData)
    fun onClosed()
    fun onCancelled()
    fun onFailure(throwable: Throwable?)
    fun onParseError(exception: Exception)
}

/**
 * Message data model
 */
data class MessageData(
    val messageId: String,
    val content: String,
    val role: String,
    val deleted: Boolean = false
)

/**
 * Message chunk data model
 */
data class MessageChunkData(
    val chunkId: String,
    val messageId: String,
    val content: String,
    val error: Boolean = false,
    val last: Boolean = false,
    val errorMessage: String? = null
)

