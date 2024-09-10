package com.jocmp.capy.common

import java.net.MalformedURLException
import java.net.URL

fun optionalURL(string: String?, baseURL: String? = null): URL? {
    if (string.isNullOrBlank()) {
        return null
    }

    return try {
        URL(string)
    } catch (_: MalformedURLException) {
        null
    }
}
