package com.jocmp.basilreader.ui.articles

import android.content.Context
import com.jocmp.basil.Article
import com.jocmp.basil.MacroProcessor
import com.jocmp.basil.R

class ArticleRenderer(
    private val article: Article,
    private val template: String,
    private val styles: String,
) {
    private val body = article.contentHTML.ifBlank {
        article.summary
    }

    fun render(): String {
        val substitutions = mapOf(
            "body" to body,
            "style" to styles
        )

        return MacroProcessor(
            template = template,
            substitutions = substitutions
        ).renderedText
    }

    companion object {
        fun render(article: Article?, context: Context): String {
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
                    ).render()
                }
            }
        }
    }
}
