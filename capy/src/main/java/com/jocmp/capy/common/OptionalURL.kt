package com.jocmp.capy.common

import java.net.URI
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
        URI(string).toURL()
    } catch (_: Throwable) {
        null
    }
}
