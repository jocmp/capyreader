package com.jocmp.mercury.extractors.generic.leadimageurl

// An ordered list of meta tag names that denote likely article leading images.
// All attributes should be lowercase for faster case-insensitive matching.
// From most distinct to least distinct.
val LEAD_IMAGE_URL_META_TAGS: List<String> =
    listOf(
        "og:image",
        "twitter:image",
        "image_src",
    )

val LEAD_IMAGE_URL_SELECTORS: List<String> = listOf("link[rel=image_src]")

val POSITIVE_LEAD_IMAGE_URL_HINTS: List<String> =
    listOf(
        "upload",
        "wp-content",
        "large",
        "photo",
        "wp-image",
    )
val POSITIVE_LEAD_IMAGE_URL_HINTS_RE: Regex =
    Regex(POSITIVE_LEAD_IMAGE_URL_HINTS.joinToString("|"), RegexOption.IGNORE_CASE)

val NEGATIVE_LEAD_IMAGE_URL_HINTS: List<String> =
    listOf(
        "spacer",
        "sprite",
        "blank",
        "throbber",
        "gradient",
        "tile",
        "bg",
        "background",
        "icon",
        "social",
        "header",
        "hdr",
        "advert",
        "spinner",
        "loader",
        "loading",
        "default",
        "rating",
        "share",
        "facebook",
        "twitter",
        "theme",
        "promo",
        "ads",
        "wp-includes",
    )
val NEGATIVE_LEAD_IMAGE_URL_HINTS_RE: Regex =
    Regex(NEGATIVE_LEAD_IMAGE_URL_HINTS.joinToString("|"), RegexOption.IGNORE_CASE)

val GIF_RE: Regex = Regex("""\.gif(\?.*)?$""", RegexOption.IGNORE_CASE)
val JPG_RE: Regex = Regex("""\.jpe?g(\?.*)?$""", RegexOption.IGNORE_CASE)
