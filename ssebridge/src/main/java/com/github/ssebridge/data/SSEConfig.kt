package com.github.ssebridge.data

import java.util.concurrent.TimeUnit

/**
 * SSE Client configuration
 *
 * Contains all configuration parameters for the SSE client.
 *
 * @property connectTimeout Connection timeout
 * @property readTimeout Read timeout
 * @property writeTimeout Write timeout
 * @property timeUnit Time unit for timeout values
 * @property enableLogging Enable logging for debugging
 */
data class SSEConfig(
    val connectTimeout: Long = DEFAULT_CONNECT_TIMEOUT,
    val readTimeout: Long = DEFAULT_READ_TIMEOUT,
    val writeTimeout: Long = DEFAULT_WRITE_TIMEOUT,
    val timeUnit: TimeUnit = TimeUnit.MINUTES,
    val enableLogging: Boolean = false
) {
    companion object {
        private const val DEFAULT_CONNECT_TIMEOUT = 1L
        private const val DEFAULT_READ_TIMEOUT = 2L
        private const val DEFAULT_WRITE_TIMEOUT = 1L

        /**
         * Create default configuration
         */
        fun createDefault(): SSEConfig {
            return SSEConfig()
        }
    }

    /**
     * Builder for SSEConfig
     */
    class Builder {
        private var connectTimeout: Long = DEFAULT_CONNECT_TIMEOUT
        private var readTimeout: Long = DEFAULT_READ_TIMEOUT
        private var writeTimeout: Long = DEFAULT_WRITE_TIMEOUT
        private var timeUnit: TimeUnit = TimeUnit.MINUTES
        private var enableLogging: Boolean = false

        fun setConnectTimeout(timeout: Long): Builder {
            this.connectTimeout = timeout
            return this
        }

        fun setReadTimeout(timeout: Long): Builder {
            this.readTimeout = timeout
            return this
        }

        fun setWriteTimeout(timeout: Long): Builder {
            this.writeTimeout = timeout
            return this
        }

        fun setTimeUnit(unit: TimeUnit): Builder {
            this.timeUnit = unit
            return this
        }

        fun setEnableLogging(enable: Boolean): Builder {
            this.enableLogging = enable
            return this
        }

        fun build(): SSEConfig {
            return SSEConfig(
                connectTimeout = connectTimeout,
                readTimeout = readTimeout,
                writeTimeout = writeTimeout,
                timeUnit = timeUnit,
                enableLogging = enableLogging
            )
        }
    }
}

