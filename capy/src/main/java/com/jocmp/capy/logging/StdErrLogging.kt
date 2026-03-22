package com.jocmp.capy.logging

class StdErrLogging : Logging {
    override fun debug(event: String, data: Map<String, Any?>) {
        System.err.println("$TAG D ${serializeData(event, data)}")
    }

    override fun info(event: String, data: Map<String, Any?>) {
        System.err.println("$TAG I ${serializeData(event, data)}")
    }

    override fun warn(event: String, data: Map<String, String?>) {
        System.err.println("$TAG W ${serializeData(event, data)}")
    }

    override fun error(
        event: String,
        error: Throwable,
        data: Map<String, Any?>
    ) {
        System.err.println("$TAG E ${serializeData(event, data)}")
        error.printStackTrace(System.err)
    }

    private fun serializeData(event: String, data: Map<String, Any?>): String {
        return "event=${event.padEnd(15, ' ')}" + data.map { (key, value) -> "$key=$value" }.joinToString(" ")
    }

    companion object {
        private const val TAG = "capy"
    }
}
