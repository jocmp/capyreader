package com.jocmp.basilreader.ui.articles.detail

import android.content.Context
import androidx.compose.runtime.Composable
import com.jocmp.basil.Article
import com.jocmp.basil.MacroProcessor
import com.jocmp.basil.R

class ArticleRenderer(
    private val article: Article,
    private val template: String,
    private val styles: String,
    private val colors: Map<String, String>
) {
    private val body = article.contentHTML.ifBlank {
        article.summary
    }

    fun render(): String {
        val substitutions = colors + mapOf(
            "external_link" to article.url.toString(),
            "title" to article.title,
            "byline" to (article.author ?: ""),
            "body" to body,
            "style" to styles
        )

        return MacroProcessor(
            template = template,
            substitutions = substitutions
        ).renderedText
    }

    companion object {
        fun render(article: Article?, colors: Map<String, String>, context: Context): String {
            if (article == null) {
                return ""
            }

            return context.resources.openRawResource(R.raw.stylesheet).use { styleStream ->
                context.resources.openRawResource(R.raw.template).use { templateStream ->
                    val template = templateStream.bufferedReader().readText()
                    val style = styleStream.bufferedReader().readText()

                    ArticleRenderer(
                        article = article,
                        template = template,
                        styles = style,
                        colors = colors
                    ).render()
                }
            }
        }
    }
}
