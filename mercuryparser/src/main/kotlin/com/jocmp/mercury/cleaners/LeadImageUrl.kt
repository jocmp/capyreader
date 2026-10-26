package com.jocmp.mercury.cleaners

import java.net.URI

// Mirrors upstream `new URL(s).toString()` — percent-encodes any non-ASCII
// path/query characters (URI.toString() would leave them raw).
fun cleanLeadImageUrl(leadImageUrl: String): String? =
    try {
        URI.create(leadImageUrl.trim()).toASCIIString()
    } catch (_: Throwable) {
        null
    }
