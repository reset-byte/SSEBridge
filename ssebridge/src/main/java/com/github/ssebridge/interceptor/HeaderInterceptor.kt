package com.github.ssebridge.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Header interceptor for SSE requests
 *
 * Adds common headers required for SSE connections.
 * Can be extended to add custom headers.
 *
 * @property additionalHeaders Additional custom headers
 */
class HeaderInterceptor(
    private val additionalHeaders: Map<String, String> = emptyMap()
) : SSEInterceptor {

    companion object {
        private const val HEADER_ACCEPT = "Accept"
        private const val HEADER_CACHE_CONTROL = "Cache-Control"
        private const val VALUE_TEXT_EVENT_STREAM = "text/event-stream"
        private const val VALUE_NO_CACHE = "no-cache"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header(HEADER_ACCEPT, VALUE_TEXT_EVENT_STREAM)
            .header(HEADER_CACHE_CONTROL, VALUE_NO_CACHE)

        additionalHeaders.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }

        return chain.proceed(requestBuilder.build())
    }
}

