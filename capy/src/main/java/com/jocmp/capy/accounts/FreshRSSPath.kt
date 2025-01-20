package com.jocmp.capy.accounts

fun withFreshRSSPath(url: String, source: Source): String {
    if (source != Source.FRESHRSS || url.endsWith(FRESHRSS_PATH)) {
        return url
    }

    return "${url}${FRESHRSS_PATH}"
}

const val FRESHRSS_PATH = "api/greader.php/"
