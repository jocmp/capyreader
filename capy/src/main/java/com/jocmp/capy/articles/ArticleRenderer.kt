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
        var totalCJK = 0
        var totalChecked = 0

        for (r in text) {
            if (totalChecked >= 50) break
            totalChecked++

            val block = Character.UnicodeBlock.of(r)
            if (block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
                block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                block == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS ||
                block == Character.UnicodeBlock.HIRAGANA ||
                block == Character.UnicodeBlock.KATAKANA ||
                block == Character.UnicodeBlock.HANGUL_SYLLABLES ||
                block == Character.UnicodeBlock.HANGUL_JAMO ||
                block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO ||
                block == Character.UnicodeBlock.BOPOMOFO ||
                block == Character.UnicodeBlock.YI_SYLLABLES ||
                block == Character.UnicodeBlock.YI_RADICALS
            ) {
                totalCJK++
            }
        }

        return totalChecked > 0 && totalCJK * 2 >= totalChecked
    }
}

private val Document.content
    get() = getElementById("article-body-content")

private fun Article.externalLink(): String {
    val potentialURL = url ?: siteURL

    return potentialURL?.toString() ?: ""
}
