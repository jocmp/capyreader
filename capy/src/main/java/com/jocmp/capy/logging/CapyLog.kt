package com.jocmp.capy.logging

import android.util.Log

object CapyLog : Logging {
    override fun debug(event: String, data: Map<String, Any?>) {
        Log.d(TAG, serializeData(event, data))
    }

    override fun info(event: String, data: Map<String, Any?>) {
        Log.i(TAG, serializeData(event, data))
    }

    override fun warn(event: String, data: Map<String, String?>) {
        Log.w(TAG, serializeData(event, data))
    }

    override fun error(
        event: String,
        error: Throwable,
        data: Map<String, Any?>
    ) {
        Log.e(TAG, serializeData(event, data), error)
    }

    private fun serializeData(event: String, data: Map<String, Any?>): String {
        return "event=${event.padEnd(15, ' ')}" + data.map { (key, value) -> "$key=$value" }.joinToString(" ")
    }

    private const val TAG = "capy"
}
