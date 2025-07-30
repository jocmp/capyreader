package com.jocmp.capy.articles

import android.content.Context
import com.jocmp.capy.Article
import com.jocmp.capy.MacroProcessor
import com.jocmp.capy.preferences.Preference
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import com.jocmp.capy.R as CapyRes

class ArticleRenderer(
    private val context: Context,
    private val textSize: Preference<Int>,
    private val fontOption: Preference<FontOption>,
    private val hideTopMargin: Preference<Boolean>,
    private val enableHorizontalScroll: Preference<Boolean>,
) {
    private val template by lazy {
        context.resources.openRawResource(CapyRes.raw.template)
            .bufferedReader()
            .readText()
    }

    fun render(
        article: Article,
        byline: String,
        colors: Map<String, String>,
        hideImages: Boolean,
    ): String {
        val fontFamily = fontOption.get()
        val showPlaceholderTitle = article.title.isBlank()

        val title = if (showPlaceholderTitle) {
            article.feedName
        } else {
            article.title
        }

        val feedName = if (showPlaceholderTitle) {
            ""
        } else {
            article.feedName
        }

        val characters = article.content.length;
        val charactersPerMinute = if (isCJK(article.content)) 265 else 500
        val readingTime = (characters + charactersPerMinute - 1) / charactersPerMinute; // Round up

        val substitutions = colors + mapOf(
            "external_link" to article.externalLink(),
            "title" to title,
            "byline" to byline,
            "feed_name" to feedName,
            "font_size" to "${textSize.get()}px",
            "font_family" to fontFamily.slug,
            "font_preload" to fontPreload(fontFamily),
            "top_margin" to topMargin(),
            "pre_white_space" to preWhiteSpace(),
            "reading_time" to "$readingTime min read"
        )

        val html = MacroProcessor(
            template = template,
            substitutions = substitutions
        ).renderedText

        val document = Jsoup.parse(html).apply {
            article.siteURL?.let { setBaseUri(it) }
        }

        if (article.parseFullContent) {
            val contentHTML = Jsoup.parse(article.content)

            HtmlPostProcessor.clean(contentHTML, hideImages = hideImages)

            document.content?.append(parseHtml(article, contentHTML, hideImages = hideImages))
        } else {
            article.imageEnclosures()?.let {
                document.content?.appendChild(it)
            }

            document.content?.append(article.content)

            HtmlPostProcessor.clean(document, hideImages = hideImages)
        }

        return document.html()
    }

    private fun topMargin(): String {
        return if (hideTopMargin.get()) {
            "0px"
        } else {
            "64px"
        }
    }

    private fun preWhiteSpace(): String {
        return if (enableHorizontalScroll.get()) {
            "pre-wrap"
        } else {
            "pre"
        }
    }

    private fun fontPreload(fontFamily: FontOption): String {
        return when (fontFamily) {
            FontOption.SYSTEM_DEFAULT -> ""
            else -> """
                <link rel="preload" href="https://appassets.androidplatform.net/res/font/${fontFamily.slug}.ttf" as="font" type="font/ttf" crossorigin>
                """.trimIndent()
        }
    }

    fun isCJK(text: String): Boolean {
        for (ch in text) {
            val code = ch.code
            if (
                (code in 0x4E00..0x9FFF) ||
                (code in 0x3400..0x4DBF) ||
                (code in 0x20000..0x2A6DF) ||
                (code in 0x3040..0x309F) ||
                (code in 0x30A0..0x30FF) ||
                (code in 0xAC00..0xD7AF)
            ) {
                return true
            }
        }
        return false
    }
}

private val Document.content
    get() = getElementById("article-body-content")

private fun Article.externalLink(): String {
    val potentialURL = url ?: siteURL

    return potentialURL?.toString() ?: ""
}
