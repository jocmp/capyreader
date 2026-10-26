package com.jocmp.mercury.utils.text

import java.nio.charset.Charset

// check a string for encoding; this is
// used in our fetchResource function to
// ensure correctly encoded responses
fun getEncoding(input: String?): String {
    var str: String? = input
    var encoding = DEFAULT_ENCODING
    val matches = input?.let { ENCODING_RE.find(it) }
    if (matches != null) {
        str = matches.groupValues[1]
    }
    if (str != null && encodingExists(str)) {
        encoding = str
    }
    return encoding
}

private fun encodingExists(name: String): Boolean =
    try {
        Charset.isSupported(name)
    } catch (_: IllegalArgumentException) {
        false
    }
