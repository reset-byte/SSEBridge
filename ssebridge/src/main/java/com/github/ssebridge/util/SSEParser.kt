package com.github.ssebridge.util

import org.json.JSONArray
import org.json.JSONObject

/**
 * SSE Data parser utility
 *
 * Provides utility methods for parsing SSE event data.
 */
object SSEParser {

    /**
     * Parse JSON object from string
     *
     * @param data JSON string
     * @return JSONObject or null if parsing fails
     */
    fun parseJsonObject(data: String): JSONObject? {
        return try {
            JSONObject(data)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse JSON array from string
     *
     * @param data JSON string
     * @return JSONArray or null if parsing fails
     */
    fun parseJsonArray(data: String): JSONArray? {
        return try {
            JSONArray(data)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if string is valid JSON
     *
     * @param data String to check
     * @return true if valid JSON, false otherwise
     */
    fun isValidJson(data: String): Boolean {
        return parseJsonObject(data) != null || parseJsonArray(data) != null
    }

    /**
     * Extract field from JSON string
     *
     * @param data JSON string
     * @param fieldName Field name to extract
     * @return Field value or null if not found
     */
    fun extractField(data: String, fieldName: String): String? {
        val jsonObject = parseJsonObject(data) ?: return null
        return if (jsonObject.has(fieldName)) {
            jsonObject.getString(fieldName)
        } else {
            null
        }
    }
}

