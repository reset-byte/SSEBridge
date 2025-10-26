package com.github.ssebridge.data

/**
 * SSE Connection state
 *
 * Represents the current state of an SSE connection.
 */
enum class SSEState {
    /** Not connected */
    IDLE,

    /** Connecting to server */
    CONNECTING,

    /** Connected and receiving events */
    CONNECTED,

    /** Connection closed normally */
    CLOSED,

    /** Connection failed with error */
    FAILED,

    /** Connection cancelled by user */
    CANCELLED
}

