package com.jocmp.capy.common

import java.net.MalformedURLException
import java.net.URL

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/Window/origin
 *
 * Returns the URL's scheme/host/port
 */
fun windowOrigin(url: URL?): String? {
    if (url == null || !(url.protocol == "http" || url.protocol == "https")) {
        return null
    }

    return try {
        URL(url.protocol, url.host, url.port, "", null).toString()
    } catch (e: MalformedURLException) {
        null
    }
}
