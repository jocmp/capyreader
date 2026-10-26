package com.jocmp.mercury.dsl

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Doc internal constructor(
    val document: Document,
    private val isFragment: Boolean = false,
) {
    init {
        document.outputSettings().prettyPrint(false)
    }

    operator fun invoke(selector: String): Selection = Selection(this, document.select(selector))

    fun root(): Selection = Selection(this, document.children())

    fun html(): String = if (isFragment) document.body().html() else document.outerHtml()

    internal fun wrap(elements: org.jsoup.select.Elements): Selection = Selection(this, elements)

    internal fun wrap(element: Element): Selection = Selection(this, org.jsoup.select.Elements(element))

    companion object {
        fun load(
            html: String,
            baseUri: String? = null,
            isDocument: Boolean = true,
        ): Doc =
            if (isDocument) {
                Doc(Jsoup.parse(html, baseUri ?: ""))
            } else {
                Doc(Jsoup.parseBodyFragment(html, baseUri ?: ""), isFragment = true)
            }
    }
}
