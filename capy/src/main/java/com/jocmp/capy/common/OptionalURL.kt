package com.jocmp.capy.common

import java.net.URL

fun URL.baseURL(): URL? {
    val port = if (port != -1) ":$port" else ""
    return optionalURL("$protocol://$host$port")
}

fun optionalURL(string: String?): URL? {
    if (string.isNullOrBlank()) {
        return null
    }

    return try {
        URL(string)
    } catch (_: Throwable) {
        null
    }
}
