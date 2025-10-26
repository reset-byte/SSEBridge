package com.github.ssebridge.extension

import android.os.Handler
import android.os.Looper
import com.github.ssebridge.data.SSEEvent
import org.json.JSONArray
import org.json.JSONObject

/**
 * Extension functions for SSE operations
 */

/**
 * Execute on main thread
 */
fun runOnMainThread(action: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        action()
    } else {
        Handler(Looper.getMainLooper()).post(action)
    }
}

/**
 * Parse SSE event data as JSONObject
 */
fun SSEEvent.asJsonObject(): JSONObject? {
    return try {
        JSONObject(data)
    } catch (e: Exception) {
        null
    }
}

/**
 * Parse SSE event data as JSONArray
 */
fun SSEEvent.asJsonArray(): JSONArray? {
    return try {
        JSONArray(data)
    } catch (e: Exception) {
        null
    }
}

/**
 * Check if SSE event data is valid JSON
 */
fun SSEEvent.isValidJson(): Boolean {
    return asJsonObject() != null || asJsonArray() != null
}

