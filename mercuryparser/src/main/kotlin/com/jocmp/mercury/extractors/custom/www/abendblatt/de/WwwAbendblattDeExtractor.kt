package com.jocmp.mercury.extractors.custom.www.abendblatt.de

import com.jocmp.mercury.dsl.Selection
import com.jocmp.mercury.extractors.TransformResult
import com.jocmp.mercury.extractors.extractor

private fun deobfuscate(node: Selection) {
    val el = node.elements.firstOrNull() ?: return
    if (!el.classNames().contains("obfuscated")) return
    val source = node.text()
    val out = StringBuilder(source.length)
    for (ch in source) {
        val r = ch.code
        when {
            r == 177 -> out.append('%')
            r == 178 -> out.append('!')
            r == 180 -> out.append(';')
            r == 181 -> out.append('=')
            r == 32 -> out.append(' ')
            r == 10 -> out.append('\n')
            r > 33 -> out.append((r - 1).toChar())
        }
    }
    el.html(out.toString())
    el.removeClass("obfuscated")
    el.addClass("deobfuscated")
}

val WwwAbendblattDeExtractor =
    extractor("www.abendblatt.de") {
        title { selectors("h2.article__header__headline") }

        author { selectors("span.author-info__name-text") }

        datePublished {
            attr("time.teaser-stream-time", "datetime")
            attr("time.article__header__date", "datetime")
        }

        dek { attr("meta[name=\"description\"]", "value") }

        leadImageUrl { attr("meta[name=\"og:image\"]", "value") }

        content {
            selectors("div.article__body")

            transform("p") { node, _ ->
                deobfuscate(node)
                TransformResult.NoChange
            }
            transform("div") { node, _ ->
                deobfuscate(node)
                TransformResult.NoChange
            }
        }
    }
