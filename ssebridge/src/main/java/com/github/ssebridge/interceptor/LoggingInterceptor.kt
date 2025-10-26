package com.github.ssebridge.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Logging interceptor for SSE requests
 *
 * Logs request and response information for debugging purposes.
 *
 * @property tag Log tag
 * @property enabled Enable/disable logging
 */
class LoggingInterceptor(
    private val tag: String = TAG,
    private val enabled: Boolean = true
) : SSEInterceptor {

    companion object {
        private const val TAG = "SSEBridge"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (enabled) {
            logRequest(request.method, request.url.toString())
        }

        val response = chain.proceed(request)

        if (enabled) {
            logResponse(response.code, response.message)
        }

        return response
    }

    private fun logRequest(method: String, url: String) {
        Log.d(tag, "→ $method $url")
    }

    private fun logResponse(code: Int, message: String) {
        Log.d(tag, "← $code $message")
    }
}

