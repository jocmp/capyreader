package com.jocmp.capy.articles

import org.jsoup.nodes.Document

internal fun wrapTables(document: Document) {
    document.select("table").forEach { table ->
        val wrapper = document.createElement("div")

        wrapper.addClass("table__wrapper")

        table.wrap(wrapper.outerHtml())
    }
}
