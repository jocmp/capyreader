package com.jocmp.rssparser.internal

internal object ImagePolicy {
    fun isValidArticleImage(src: String): Boolean {
        return src.isNotBlank() &&
                !src.contains(EMOJI_DOMAIN) &&
                !src.contains(GRAVATAR_DOMAIN)
    }

    private const val EMOJI_DOMAIN = "s.w.org/images/core/emoji"
    private const val GRAVATAR_DOMAIN = "gravatar.com"
}
