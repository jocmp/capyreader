package com.capyreader.app.common

import android.util.Log
import com.jocmp.capy.logging.Logging

class AndroidLogging : Logging {
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

    companion object {
        private const val TAG = "capy"
    }
}
