package com.jocmp.capy.logging

interface Logging {
    fun debug(event: String, data: Map<String, Any?> = emptyMap())

    fun info(event: String, data: Map<String, Any?> = emptyMap())

    fun warn(event: String, data: Map<String, String?> = emptyMap())

    fun error(event: String, error: Throwable, data: Map<String, Any?> = emptyMap())

    fun measure(event: String, data: Map<String, Any?> = emptyMap(), block: () -> Unit) {
        block()
    }

    suspend fun measureAsync(event: String, data: Map<String, Any?> = emptyMap(), block: suspend () -> Unit) {
        block()
    }
}
