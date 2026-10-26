package com.jocmp.mercury.cleaners

import com.jocmp.mercury.dsl.Doc
import com.jocmp.mercury.dsl.Selection
import com.jocmp.mercury.utils.dom.cleanAttributes
import com.jocmp.mercury.utils.dom.cleanHOnes
import com.jocmp.mercury.utils.dom.cleanHeaders
import com.jocmp.mercury.utils.dom.cleanImages
import com.jocmp.mercury.utils.dom.cleanTags
import com.jocmp.mercury.utils.dom.makeLinksAbsolute
import com.jocmp.mercury.utils.dom.markToKeep
import com.jocmp.mercury.utils.dom.removeEmpty
import com.jocmp.mercury.utils.dom.rewriteTopLevel
import com.jocmp.mercury.utils.dom.stripJunkTags

// Clean our article content, returning a new, cleaned node.
fun extractCleanNode(
    article: Selection,
    doc: Doc,
    cleanConditionally: Boolean = true,
    title: String = "",
    url: String = "",
    defaultCleaner: Boolean = true,
): Selection {
    // Rewrite the tag name to div if it's a top level node like body or
    // html to avoid later complications with multiple body tags.
    rewriteTopLevel(article, doc)

    // Drop small images and spacer images
    // Only do this is defaultCleaner is set to true;
    // this can sometimes be too aggressive.
    if (defaultCleaner) cleanImages(article, doc)

    // Make links absolute
    makeLinksAbsolute(article, doc, url)

    // Mark elements to keep that would normally be removed.
    // E.g., stripJunkTags will remove iframes, so we're going to mark
    // YouTube/Vimeo videos as elements we want to keep.
    markToKeep(article, doc, url)

    // Drop certain tags like <title>, etc
    // This is -mostly- for cleanliness, not security.
    stripJunkTags(article, doc)

    // H1 tags are typically the article title, which should be extracted
    // by the title extractor instead. If there's less than 3 of them (<3),
    // strip them. Otherwise, turn 'em into H2s.
    cleanHOnes(article, doc)

    // Clean headers
    cleanHeaders(article, doc, title)

    // We used to clean UL's and OL's here, but it was leading to
    // too many in-article lists being removed. Consider a better
    // way to detect menus particularly and remove them.
    // Also optionally running, since it can be overly aggressive.
    // Upstream passes cleanConditionally through to cleanTags, but cleanTags
    // does not consume the argument. Preserved as a no-op parameter for parity.
    @Suppress("UNUSED_VARIABLE")
    val ignored = cleanConditionally
    if (defaultCleaner) cleanTags(article, doc)

    // Remove empty paragraph nodes
    removeEmpty(article, doc)

    // Remove unnecessary attributes
    cleanAttributes(article, doc)

    return article
}
