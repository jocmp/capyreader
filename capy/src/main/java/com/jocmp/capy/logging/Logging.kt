package com.jocmp.capy.logging

interface Logging {
    fun debug(event: String, data: Map<String, Any?> = emptyMap())

    fun info(event: String, data: Map<String, Any?> = emptyMap())

    fun warn(event: String, data: Map<String, String?> = emptyMap())

    fun error(event: String, error: Throwable, data: Map<String, Any?> = emptyMap())
}
