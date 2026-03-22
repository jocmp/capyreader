package com.jocmp.capy.logging

object CapyLog : Logging {
    private var delegate: Logging = StdErrLogging()

    fun install(logging: Logging) {
        delegate = logging
    }

    override fun debug(event: String, data: Map<String, Any?>) {
        delegate.debug(event, data)
    }

    override fun info(event: String, data: Map<String, Any?>) {
        delegate.info(event, data)
    }

    override fun warn(event: String, data: Map<String, String?>) {
        delegate.warn(event, data)
    }

    override fun error(
        event: String,
        error: Throwable,
        data: Map<String, Any?>
    ) {
        delegate.error(event, error, data)
    }
}
