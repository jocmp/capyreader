package com.jocmp.capy.logging

interface Logging {
    fun info(tag: String, data: Map<String, String?> = emptyMap())

    fun warn(tag: String, data: Map<String, String?> = emptyMap())

    fun error(tag: String, error: Throwable, data: Map<String, String?> = emptyMap())
}
