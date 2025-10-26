package com.github.ssebridge.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.github.ssebridge.data.SSEConfig
import com.github.ssebridge.data.SSEEvent
import com.github.ssebridge.data.SSEMethod
import com.github.ssebridge.data.SSERequest
import com.github.ssebridge.data.SSEState
import com.github.ssebridge.interceptor.HeaderInterceptor
import com.github.ssebridge.interceptor.LoggingInterceptor
import com.github.ssebridge.interceptor.SSEInterceptor
import com.github.ssebridge.listener.SSEEventListener
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.http2.StreamResetException
import okhttp3.internal.sse.RealEventSource
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import java.util.concurrent.atomic.AtomicReference

/**
 * SSE Client
 *
 * Main class for establishing and managing SSE connections.
 * Supports lifecycle management, custom interceptors, and configuration.
 *
 * @property config SSE configuration
 * @property lifecycle Optional lifecycle for automatic connection management
 */
class SSEClient private constructor(
    private val config: SSEConfig,
    private val lifecycle: Lifecycle? = null
) {

    companion object {
        private const val CONTENT_TYPE_JSON = "application/json; charset=utf-8"
    }

    /** Current connection state */
    private val currentState = AtomicReference(SSEState.IDLE)

    /** Event source instance */
    private var eventSource: RealEventSource? = null

    /** OkHttp client instance */
    private var okHttpClient: OkHttpClient? = null

    /** Custom interceptors */
    private val interceptors = mutableListOf<SSEInterceptor>()

    /** Event listener */
    private var eventListener: SSEEventListener? = null

    init {
        initializeLifecycleObserver()
    }

    /**
     * Initialize lifecycle observer for automatic cleanup
     */
    private fun initializeLifecycleObserver() {
        lifecycle?.addObserver(LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) {
                disconnect()
                cleanup()
            }
        })
    }

    /**
     * Add custom interceptor
     *
     * @param interceptor SSE interceptor
     */
    fun addInterceptor(interceptor: SSEInterceptor): SSEClient {
        interceptors.add(interceptor)
        return this
    }

    /**
     * Set event listener
     *
     * @param listener SSE event listener
     */
    fun setEventListener(listener: SSEEventListener): SSEClient {
        this.eventListener = listener
        return this
    }

    /**
     * Connect to SSE server
     *
     * @param request SSE request configuration
     */
    fun connect(request: SSERequest) {
        if (isConnecting()) {
            return
        }
        updateState(SSEState.CONNECTING)
        val httpClient = buildOkHttpClient()
        val httpRequest = buildRequest(request)
        eventSource = RealEventSource(httpRequest, createEventSourceListener())
        eventSource?.connect(httpClient)
    }

    /**
     * Build OkHttp client with configuration and interceptors
     */
    private fun buildOkHttpClient(): OkHttpClient {
        if (okHttpClient != null) {
            return okHttpClient!!
        }
        val builder = OkHttpClient.Builder()
            .connectTimeout(config.connectTimeout, config.timeUnit)
            .readTimeout(config.readTimeout, config.timeUnit)
            .writeTimeout(config.writeTimeout, config.timeUnit)
        if (config.enableLogging) {
            builder.addInterceptor(LoggingInterceptor(enabled = true))
        }
        builder.addInterceptor(HeaderInterceptor())
        interceptors.forEach { builder.addInterceptor(it) }
        okHttpClient = builder.build()
        return okHttpClient!!
    }

    /**
     * Build OkHttp request from SSE request
     */
    private fun buildRequest(sseRequest: SSERequest): Request {
        val builder = Request.Builder()
            .url(sseRequest.url)
        val headers = sseRequest.buildHeaders()
        builder.headers(headers)
        when (sseRequest.method) {
            SSEMethod.GET -> builder.get()
            SSEMethod.POST -> {
                val body = sseRequest.body?.toByteArray()
                    ?.toRequestBody(CONTENT_TYPE_JSON.toMediaTypeOrNull())
                    ?: ByteArray(0).toRequestBody(CONTENT_TYPE_JSON.toMediaTypeOrNull())
                builder.post(body)
            }
        }
        return builder.build()
    }

    /**
     * Create event source listener
     */
    private fun createEventSourceListener(): EventSourceListener {
        return object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                super.onOpen(eventSource, response)
                if (isDestroyed()) return
                updateState(SSEState.CONNECTED)
                eventListener?.onConnected()
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                super.onEvent(eventSource, id, type, data)
                if (isDestroyed()) return
                val event = SSEEvent(
                    id = id,
                    type = type,
                    data = data
                )
                eventListener?.onEvent(event)
            }

            override fun onClosed(eventSource: EventSource) {
                super.onClosed(eventSource)
                if (isDestroyed()) return
                updateState(SSEState.CLOSED)
                eventListener?.onClosed()
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                super.onFailure(eventSource, t, response)
                if (isDestroyed()) return
                when {
                    (t as? StreamResetException)?.message?.contains("cancel", true) == true -> {
                        updateState(SSEState.CANCELLED)
                        eventListener?.onCancelled()
                    }
                    else -> {
                        updateState(SSEState.FAILED)
                        eventListener?.onFailure(t)
                    }
                }
            }
        }
    }

    /**
     * Disconnect from SSE server
     */
    fun disconnect() {
        eventSource?.cancel()
        if (currentState.get() == SSEState.CONNECTED || currentState.get() == SSEState.CONNECTING) {
            updateState(SSEState.CANCELLED)
        }
    }

    /**
     * Check if currently connecting or connected
     */
    fun isConnecting(): Boolean {
        val state = currentState.get()
        return state == SSEState.CONNECTING || state == SSEState.CONNECTED
    }

    /**
     * Get current connection state
     */
    fun getState(): SSEState {
        return currentState.get()
    }

    /**
     * Update connection state
     */
    private fun updateState(newState: SSEState) {
        currentState.set(newState)
        eventListener?.onStateChanged(newState)
    }

    /**
     * Check if lifecycle is destroyed
     */
    private fun isDestroyed(): Boolean {
        return lifecycle?.currentState == Lifecycle.State.DESTROYED
    }

    /**
     * Cleanup resources
     */
    private fun cleanup() {
        eventSource = null
        eventListener = null
        interceptors.clear()
        okHttpClient = null
    }

    /**
     * Builder for SSEClient
     */
    class Builder {
        private var config: SSEConfig = SSEConfig.createDefault()
        private var lifecycle: Lifecycle? = null
        private val interceptors = mutableListOf<SSEInterceptor>()

        fun setConfig(config: SSEConfig): Builder {
            this.config = config
            return this
        }

        fun setLifecycle(lifecycle: Lifecycle): Builder {
            this.lifecycle = lifecycle
            return this
        }

        fun addInterceptor(interceptor: SSEInterceptor): Builder {
            interceptors.add(interceptor)
            return this
        }

        fun build(): SSEClient {
            val client = SSEClient(config, lifecycle)
            interceptors.forEach { client.addInterceptor(it) }
            return client
        }
    }
}

