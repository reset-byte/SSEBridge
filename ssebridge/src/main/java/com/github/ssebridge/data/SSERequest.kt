package com.github.ssebridge.data

import okhttp3.Headers

/**
 * SSE Request configuration
 *
 * Contains all parameters needed to establish an SSE connection.
 *
 * @property url Target URL for SSE connection
 * @property method HTTP method (GET or POST)
 * @property headers Custom HTTP headers
 * @property body Request body for POST requests
 */
data class SSERequest(
    val url: String,
    val method: SSEMethod = SSEMethod.GET,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
) {
    init {
        require(url.isNotBlank()) { "URL cannot be blank" }
        if (method == SSEMethod.POST) {
            require(body != null) { "Body is required for POST requests" }
        }
    }

    /**
     * Convert custom headers to OkHttp Headers
     */
    fun buildHeaders(): Headers {
        return Headers.Builder().apply {
            headers.forEach { (key, value) ->
                add(key, value)
            }
        }.build()
    }
}

/**
 * HTTP methods supported for SSE requests
 */
enum class SSEMethod {
    GET,
    POST
}

