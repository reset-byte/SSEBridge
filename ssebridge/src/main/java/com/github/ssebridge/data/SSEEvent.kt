package com.github.ssebridge.data

/**
 * SSE Event data model
 *
 * Represents a Server-Sent Event received from the server.
 *
 * @property id Event ID
 * @property type Event type
 * @property data Event data content
 * @property retry Retry interval in milliseconds
 */
data class SSEEvent(
    val id: String? = null,
    val type: String? = null,
    val data: String,
    val retry: Long? = null
)

