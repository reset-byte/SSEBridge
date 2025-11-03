package com.github.ssebridge.widget

/**
 * StreamState - Enum class for stream display states
 * 
 * Represents the different states of the streaming text display:
 * - IDLE: Initial state, waiting to start, showing blinking cursor
 * - STREAMING: Currently displaying text character by character with blinking cursor
 * - COMPLETED: Text display finished, cursor hidden
 */
enum class StreamState {
    /**
     * Initial waiting state
     * Shows blinking cursor without text
     */
    IDLE,
    
    /**
     * Text is being displayed character by character
     * Shows text with blinking cursor at the end
     */
    STREAMING,
    
    /**
     * Text display completed
     * Shows full text without cursor
     */
    COMPLETED
}

