package com.jocmp.feedfinder

import java.net.URI
import java.net.URL

internal fun optionalURL(string: String?): URL? {
    if (string.isNullOrBlank()) {
        return null
    }

    return try {
        URI(string).toURL()
    } catch (_: Throwable) {
        null
    }
}
