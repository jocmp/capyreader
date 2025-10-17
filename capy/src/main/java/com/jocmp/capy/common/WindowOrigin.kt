package com.jocmp.capy.common

import java.net.MalformedURLException
import java.net.URL

/**
 * List of hosts that should skip the site's origin
 * so that images will load correctly
 */
val skipOrigin = listOf(
    "www.qbitai.com"
)

/**
 * https://developer.mozilla.org/en-US/docs/Web/API/Window/origin
 *
 * Returns the URL's scheme/host/port
 */
fun windowOrigin(url: URL?): String? {
    if (url == null || !(url.protocol == "http" || url.protocol == "https")) {
        return null
    }

    if (skipOrigin.contains(url.host)) {
        return null
    }

    return try {
        URL(url.protocol, url.host, url.port, "", null).toString()
    } catch (e: MalformedURLException) {
        null
    }
}
