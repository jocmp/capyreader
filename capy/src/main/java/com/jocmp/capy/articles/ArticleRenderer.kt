package com.jocmp.capy.articles

import android.content.Context
import com.jocmp.capy.Article
import com.jocmp.capy.preferences.Preference
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
        colors: TemplateColors,
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

        val content = buildContent(article, hideImages)

        return String.format(
            template,
            colors.primary,
            colors.surface,
            colors.surfaceContainerHighest,
            colors.onSurface,
            colors.onSurfaceVariant,
            colors.surfaceVariant,
            colors.primaryContainer,
            colors.onPrimaryContainer,
            colors.secondary,
            colors.surfaceContainer,
            colors.surfaceTint,
            topMargin(),
            "${textSize.get()}px",
            preWhiteSpace(),
            fontPreload(fontFamily),
            article.externalLink(),
            title,
            byline,
            feedName,
            fontFamily.slug,
            content,
        )
    }

    private fun buildContent(article: Article, hideImages: Boolean): String {
        return if (article.parseFullContent) {
            parseHtml(article, hideImages)
        } else {
            val enclosures = article.imageEnclosuresHtml()
            enclosures + article.content + postProcessScript(article, hideImages)
        }
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
                """
        }
    }
}

private fun Article.externalLink(): String {
    val potentialURL = url ?: siteURL

    return potentialURL?.toString() ?: ""
}
