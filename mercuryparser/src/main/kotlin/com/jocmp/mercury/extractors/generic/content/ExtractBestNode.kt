package com.jocmp.mercury.extractors.generic.content

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection
import com.jocmp.mercury.extractors.generic.content.scoring.findTopCandidate
import com.jocmp.mercury.extractors.generic.content.scoring.scoreContent
import com.jocmp.mercury.utils.dom.convertToParagraphs
import com.jocmp.mercury.utils.dom.stripUnlikelyCandidates

// Using a variety of scoring techniques, extract the content most
// likely to be article text.
//
// If strip_unlikely_candidates is True, remove any elements that
// match certain criteria first. (Like, does this element have a
// classname of "comment")
//
// If weight_nodes is True, use classNames and IDs to determine the
// worthiness of nodes.
//
// Returns a cheerio object $
fun extractBestNode(
    doc: Doc,
    opts: ContentOptions,
): Selection {
    var d = doc
    if (opts.stripUnlikelyCandidates) {
        d = stripUnlikelyCandidates(d)
    }

    d = convertToParagraphs(d)
    d = scoreContent(d, opts.weightNodes)
    return findTopCandidate(d)
}
