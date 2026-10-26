package com.jocmp.mercury.extractors.generic.content

import com.jocmp.mercury.cleaners.extractCleanNode
import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection
import com.jocmp.mercury.utils.dom.nodeIsSufficient
import com.jocmp.mercury.utils.text.normalizeSpaces

data class ContentOptions(
    val stripUnlikelyCandidates: Boolean = true,
    val weightNodes: Boolean = true,
    val cleanConditionally: Boolean = true,
)

// Extract the content for this resource - initially, pass in our
// most restrictive opts which will return the highest quality
// content. On each failure, retry with slightly more lax opts.
//
// :param return_type: string. If "node", should return the content
// as a cheerio node rather than as an HTML string.
//
// Opts:
// stripUnlikelyCandidates: Remove any elements that match
// non-article-like criteria first.(Like, does this element
//   have a classname of "comment")
//
// weightNodes: Modify an elements score based on whether it has
// certain classNames or IDs. Examples: Subtract if a node has
// a className of 'comment', Add if a node has an ID of
// 'entry-content'.
//
// cleanConditionally: Clean the node to return of some
// superfluous content. Things like forms, ads, etc.
fun extractGenericContent(
    docIn: Doc?,
    html: String?,
    title: String,
    url: String,
    opts: ContentOptions = ContentOptions(),
): String? {
    var doc = docIn ?: html?.let { Doc.load(it) } ?: return null
    var currentOpts = opts

    var node: Selection? = getContentNode(doc, title, url, currentOpts)

    if (node != null && nodeIsSufficient(node)) {
        return cleanAndReturnNode(node)
    }

    // We didn't succeed on first pass, one by one disable our
    // extraction opts and try again.
    val passes =
        listOf(
            { currentOpts = currentOpts.copy(stripUnlikelyCandidates = false) },
            { currentOpts = currentOpts.copy(weightNodes = false) },
            { currentOpts = currentOpts.copy(cleanConditionally = false) },
        )

    for (disable in passes) {
        disable()
        // Reload doc since previous pass mutated it.
        if (html != null) {
            doc = Doc.load(html)
        }
        node = getContentNode(doc, title, url, currentOpts)
        if (node != null && nodeIsSufficient(node)) {
            break
        }
    }

    return node?.let { cleanAndReturnNode(it) }
}

// Get node given current options
private fun getContentNode(
    doc: Doc,
    title: String,
    url: String,
    opts: ContentOptions,
): Selection? {
    val best = extractBestNode(doc, opts)
    return extractCleanNode(best, doc, cleanConditionally = opts.cleanConditionally, title = title, url = url)
}

// Once we got here, either we're at our last-resort node, or
// we broke early. Make sure we at least have -something- before we
// move forward.
private fun cleanAndReturnNode(node: Selection): String? {
    if (node.length == 0) return null
    return normalizeSpaces(node.outerHtml())
}
