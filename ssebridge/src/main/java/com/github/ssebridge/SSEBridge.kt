package com.github.ssebridge

import androidx.lifecycle.Lifecycle
import com.github.ssebridge.core.SSEClient
import com.github.ssebridge.data.SSEConfig
import com.github.ssebridge.data.SSEMethod
import com.github.ssebridge.data.SSERequest
import com.github.ssebridge.interceptor.SSEInterceptor
import com.github.ssebridge.listener.SSEEventListener

/**
 * SSEBridge - Main entry point for the SSE Bridge SDK
 *
 * Provides a simplified interface for creating and managing SSE connections.
 * This is the recommended way to use the SDK.
 *
 * Example usage:
 * ```kotlin
 * val bridge = SSEBridge.Builder()
 *     .setConfig(config)
 *     .setLifecycle(lifecycle)
 *     .setEventListener(listener)
 *     .build()
 *
 * val request = SSERequest(
 *     url = "https://example.com/sse",
 *     method = SSEMethod.POST,
 *     body = jsonBody
 * )
 *
 * bridge.connect(request)
 * ```
 */
class SSEBridge private constructor(
    private val client: SSEClient
) {

    /**
     * Connect to SSE server with GET request
     *
     * @param url Target URL
     * @param headers Optional custom headers
     */
    fun connectGet(
        url: String,
        headers: Map<String, String> = emptyMap()
    ) {
        val request = SSERequest(
            url = url,
            method = SSEMethod.GET,
            headers = headers
        )
        client.connect(request)
    }

    /**
     * Connect to SSE server with POST request
     *
     * @param url Target URL
     * @param body Request body
     * @param headers Optional custom headers
     */
    fun connectPost(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap()
    ) {
        val request = SSERequest(
            url = url,
            method = SSEMethod.POST,
            headers = headers,
            body = body
        )
        client.connect(request)
    }

    /**
     * Connect to SSE server with custom request
     *
     * @param request SSE request configuration
     */
    fun connect(request: SSERequest) {
        client.connect(request)
    }

    /**
     * Disconnect from SSE server
     */
    fun disconnect() {
        client.disconnect()
    }

    /**
     * Check if currently connecting or connected
     */
    fun isConnecting(): Boolean {
        return client.isConnecting()
    }

    /**
     * Get current connection state
     */
    fun getState() = client.getState()

    /**
     * Builder for SSEBridge
     */
    class Builder {
        private var config: SSEConfig = SSEConfig.createDefault()
        private var lifecycle: Lifecycle? = null
        private var eventListener: SSEEventListener? = null
        private val interceptors = mutableListOf<SSEInterceptor>()

        /**
         * Set SSE configuration
         */
        fun setConfig(config: SSEConfig): Builder {
            this.config = config
            return this
        }

        /**
         * Set lifecycle for automatic connection management
         */
        fun setLifecycle(lifecycle: Lifecycle): Builder {
            this.lifecycle = lifecycle
            return this
        }

        /**
         * Set event listener
         */
        fun setEventListener(listener: SSEEventListener): Builder {
            this.eventListener = listener
            return this
        }

        /**
         * Add custom interceptor
         */
        fun addInterceptor(interceptor: SSEInterceptor): Builder {
            interceptors.add(interceptor)
            return this
        }

        /**
         * Build SSEBridge instance
         */
        fun build(): SSEBridge {
            val clientBuilder = SSEClient.Builder()
                .setConfig(config)
            lifecycle?.let { clientBuilder.setLifecycle(it) }
            interceptors.forEach { clientBuilder.addInterceptor(it) }
            val client = clientBuilder.build()
            eventListener?.let { client.setEventListener(it) }
            return SSEBridge(client)
        }
    }
}

