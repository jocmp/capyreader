package com.jocmp.capy.common

import java.net.URL

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
