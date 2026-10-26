package com.jocmp.mercury.utils.dom

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection
import java.net.URI

private fun resolve(
    base: String?,
    ref: String,
): String =
    try {
        URI.create(base ?: "").resolve(ref).toString()
    } catch (_: Throwable) {
        ref
    }

private fun absolutize(
    doc: Doc,
    nodes: Selection,
    rootUrl: String,
    attr: String,
) {
    val baseUrl = doc("base").attr("href")

    nodes.each { _, node ->
        val attrs = getAttrs(node)
        val url = attrs[attr] ?: return@each
        val absoluteUrl = resolve(baseUrl ?: rootUrl, url)
        setAttr(node, attr, absoluteUrl)
    }
}

private val SRCSET_CANDIDATE_RE: Regex = Regex("""(?:\s*)(\S+(?:\s*[\d.]+[wx])?)(?:\s*,\s*)?""")

private fun absolutizeSet(
    doc: Doc,
    rootUrl: String,
    content: Selection,
) {
    content.find("[srcset]").each { _, node ->
        val attrs = getAttrs(node)
        val urlSet = attrs["srcset"] ?: return@each

        val candidates =
            SRCSET_CANDIDATE_RE.findAll(urlSet)
                .map { it.value }
                .filter { it.isNotBlank() }
                .toList()
        if (candidates.isEmpty()) return@each

        val absoluteCandidates =
            candidates.map { candidate ->
                val parts = candidate.trim().replace(Regex(",$"), "").split(Regex("""\s+""")).toMutableList()
                parts[0] = resolve(rootUrl, parts[0])
                parts.joinToString(" ")
            }
        val absoluteUrlSet = absoluteCandidates.distinct().joinToString(", ")
        setAttr(node, "srcset", absoluteUrlSet)
    }
}

fun makeLinksAbsolute(
    content: Selection,
    doc: Doc,
    url: String,
): Selection {
    listOf("href", "src").forEach { attr ->
        absolutize(doc, doc("[$attr]"), url, attr)
        // Custom extractor content may be a detached clone the document query can't reach
        absolutize(doc, content.find("[$attr]"), url, attr)
    }
    absolutizeSet(doc, url, content)
    return content
}
