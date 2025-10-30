package com.jocmp.capy.articles

import com.jocmp.capy.Article


val Article.extraText: String?
    get() {
        val url = url ?: return null

        return listOf(title, url.toString())
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }
