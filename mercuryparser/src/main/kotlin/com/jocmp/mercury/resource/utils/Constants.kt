package com.jocmp.mercury.resource.utils

// Browser does not like us setting user agent
val REQUEST_HEADERS: Map<String, String> =
    mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36",
    )

// The number of milliseconds to attempt to fetch a resource before timing out.
const val FETCH_TIMEOUT: Long = 10_000

// Content types that we do not extract content from
private val BAD_CONTENT_TYPES =
    listOf(
        "audio/mpeg",
        "image/gif",
        "image/jpeg",
        "image/jpg",
    )

val BAD_CONTENT_TYPES_RE: Regex = Regex("^(${BAD_CONTENT_TYPES.joinToString("|")})$", RegexOption.IGNORE_CASE)

// Use this setting as the maximum size an article can be
// for us to attempt parsing. Defaults to 5 MB.
const val MAX_CONTENT_LENGTH: Long = 5_242_880
