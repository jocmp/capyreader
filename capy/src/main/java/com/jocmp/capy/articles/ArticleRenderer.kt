package com.jocmp.capy.articles

import android.content.Context
import com.jocmp.capy.Article
import com.jocmp.capy.MacroProcessor
import com.jocmp.capy.preferences.Preference
import org.jsoup.Jsoup
import com.jocmp.capy.R as CapyRes

class ArticleRenderer(
    private val context: Context,
    private val textSize: Preference<TextSize>,
    private val fontOption: Preference<FontOption>,
    private val hideTopMargin: Preference<Boolean>,
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

        val substitutions = colors + mapOf(
            "external_link" to article.externalLink(),
            "title" to article.title,
            "byline" to byline,
            "feed_name" to article.feedName,
            "text_size" to textSize.get().slug,
            "font_family" to fontFamily.slug,
            "font_preload" to fontPreload(fontFamily),
            "top_margin" to topMargin()
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

            document.getElementById("article-body-content")
                ?.append(parseHtml(article, contentHTML, hideImages = hideImages))
        } else {
            document.getElementById("article-body-content")?.append(article.content)

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

    private fun fontPreload(fontFamily: FontOption): String {
        return when (fontFamily) {
            FontOption.SYSTEM_DEFAULT -> ""
            else -> """
                    <link rel="preload" href="https://appassets.androidplatform.net/res/font/${fontFamily.slug}.ttf" as="font" type="font/ttf" crossorigin>
                """.trimIndent()
        }
    }
}

private fun Article.externalLink(): String {
    val potentialURL = url ?: siteURL

    return potentialURL?.toString() ?: ""
}
