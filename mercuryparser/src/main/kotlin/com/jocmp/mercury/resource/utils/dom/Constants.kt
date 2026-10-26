package com.jocmp.mercury.resource.utils.dom

val IS_LINK: Regex = Regex("https?://", RegexOption.IGNORE_CASE)
private const val IMAGE_RE: String = ".(png|gif|jpe?g)"
val IS_IMAGE: Regex = Regex(IMAGE_RE, RegexOption.IGNORE_CASE)
val IS_SRCSET: Regex = Regex("""$IMAGE_RE(\?\S+)?(\s*[\d.]+[wx])""", RegexOption.IGNORE_CASE)

val TAGS_TO_REMOVE: String = listOf("script", "style", "form").joinToString(",")
