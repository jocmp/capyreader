package com.jocmp.feedfinder

import java.net.MalformedURLException
import java.net.URL

fun optionalURL(string: String?): URL? {
    if (string.isNullOrBlank()) {
        return null
    }

    return try {
        URL(string)
    } catch (_: MalformedURLException) {
        null
    }
}
