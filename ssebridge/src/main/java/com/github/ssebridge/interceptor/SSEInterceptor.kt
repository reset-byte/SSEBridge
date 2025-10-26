package com.github.ssebridge.interceptor

import okhttp3.Interceptor

/**
 * SSE Interceptor interface
 *
 * Marker interface for SSE-specific OkHttp interceptors.
 * Implementers can intercept and modify requests/responses.
 */
interface SSEInterceptor : Interceptor

