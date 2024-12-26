package com.jocmp.capy.logging

import android.util.Log

object CapyLog : Logging {
    override fun info(tag: String, data: Map<String, String?>) {
        Log.i(appTag(tag), serializeData(data))
    }

    override fun warn(tag: String, data: Map<String, String?>) {
        Log.i(appTag(tag), serializeData(data))
    }

    override fun error(tag: String, error: Throwable) {
        Log.e(appTag(tag), "handled_exception", error)
    }

    private fun serializeData(data: Map<String, String?>): String {
        return data.map { (key, value) -> "$key=$value" }.joinToString(" ")
    }

    private fun appTag(path: String) = "cr.$path"
}
