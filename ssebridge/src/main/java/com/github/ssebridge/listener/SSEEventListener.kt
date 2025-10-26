package com.github.ssebridge.listener

import com.github.ssebridge.data.SSEEvent
import com.github.ssebridge.data.SSEState

/**
 * SSE Event listener interface
 *
 * Defines callback methods for SSE connection lifecycle and events.
 */
interface SSEEventListener {

    /**
     * Called when connection state changes
     *
     * @param state New connection state
     */
    fun onStateChanged(state: SSEState) {}

    /**
     * Called when SSE connection is established
     */
    fun onConnected() {}

    /**
     * Called when an SSE event is received
     *
     * @param event SSE event data
     */
    fun onEvent(event: SSEEvent)

    /**
     * Called when SSE connection is closed normally
     */
    fun onClosed() {}

    /**
     * Called when SSE connection fails
     *
     * @param throwable Error information
     */
    fun onFailure(throwable: Throwable?) {}

    /**
     * Called when SSE connection is cancelled by user
     */
    fun onCancelled() {}
}

