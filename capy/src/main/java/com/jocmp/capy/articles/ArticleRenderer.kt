package com.jocmp.capy.articles

import android.content.Context
import com.jocmp.capy.Article
import com.jocmp.capy.MacroProcessor
import com.jocmp.capy.R as CapyRes

data class RendererPreferences(
    val textSize: Int = FontSize.DEFAULT,
    val fontOption: FontOption = FontOption.default,
    val titleFontSize: Int = FontSize.TITLE_DEFAULT,
    val textAlignment: TextAlignment = TextAlignment.default,
    val titleFollowsBodyFont: Boolean = false,
    val hideTopMargin: Boolean = false,
    val enableHorizontalScroll: Boolean = false,
)

class ArticleRenderer(
    private val context: Context,
    private val audioPlayerLabels: AudioPlayerLabels = AudioPlayerLabels(),
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
        preferences: RendererPreferences,
    ): String {
        val fontFamily = preferences.fontOption
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

        val titleFontFamily = if (preferences.titleFollowsBodyFont) {
            fontFamily
        } else {
            FontOption.SYSTEM_DEFAULT
        }

        val substitutions = colors + mapOf(
            "external_link" to article.externalLink(),
            "title" to title,
            "byline" to byline,
            "feed_name" to feedName,
            "font_size" to "${preferences.textSize}px",
            "font_family" to fontFamily.slug,
            "font_preload" to fontPreload(fontFamily),
            "top_margin" to topMargin(preferences.hideTopMargin),
            "pre_white_space" to preWhiteSpace(preferences.enableHorizontalScroll),
            "title_font_size" to "${preferences.titleFontSize}px",
            "title_text_align" to preferences.textAlignment.toCSS,
            "title_font_family" to titleFontFamily.slug,
            "body" to content,
        )

        return MacroProcessor(template, substitutions).renderedText
    }

    private fun buildContent(article: Article, hideImages: Boolean): String {
        return if (article.parseFullContent) {
            parseHtml(article, hideImages)
        } else {
            val audioEnclosures = article.audioEnclosureHTML(
                playLabel = audioPlayerLabels.play,
                pauseLabel = audioPlayerLabels.pause,
            )
            val otherEnclosures = article.enclosureHTML()
            audioEnclosures + article.content + otherEnclosures + postProcessScript(article, hideImages)
        }
    }

    private fun topMargin(hide: Boolean): String {
        return if (hide) "0px" else "100px"
    }

    private fun preWhiteSpace(enableHorizontalScroll: Boolean): String {
        return if (enableHorizontalScroll) "pre-wrap" else "pre"
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

data class AudioPlayerLabels(
    val play: String = "Play",
    val pause: String = "Pause",
)
