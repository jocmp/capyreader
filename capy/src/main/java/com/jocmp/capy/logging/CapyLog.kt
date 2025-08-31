package com.jocmp.capy.logging

import android.util.Log

object CapyLog : Logging {
    override fun info(tag: String, data: Map<String, Any?>) {
        Log.i(appTag(tag), serializeData(data))
    }

    override fun warn(tag: String, data: Map<String, String?>) {
        Log.w(appTag(tag), serializeData(data))
    }

    override fun error(tag: String, error: Throwable, data: Map<String, String?>) {
        Log.e(appTag(tag), serializeData(data), error)
    }

    private fun serializeData(data: Map<String, Any?>): String {
        return data.map { (key, value) -> "$key=$value" }.joinToString(" ")
    }

    private fun appTag(path: String) = "cr.$path"
}
