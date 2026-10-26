package com.jocmp.mercury.extractors.generic.leadimageurl

import com.jocmp.mercury.dsl.Selection
import com.jocmp.mercury.extractors.generic.content.scoring.PHOTO_HINTS_RE
import kotlin.math.roundToInt

private fun getSig(node: Selection): String {
    val klass = node.attr("class") ?: ""
    val id = node.attr("id") ?: ""
    return "$klass $id"
}

// Scores image urls based on a variety of heuristics.
fun scoreImageUrl(url: String): Int {
    val u = url.trim()
    var score = 0

    if (POSITIVE_LEAD_IMAGE_URL_HINTS_RE.containsMatchIn(u)) {
        score += 20
    }

    if (NEGATIVE_LEAD_IMAGE_URL_HINTS_RE.containsMatchIn(u)) {
        score -= 20
    }

    // TODO: We might want to consider removing this as
    // gifs are much more common/popular than they once were
    if (GIF_RE.containsMatchIn(u)) {
        score -= 10
    }

    if (JPG_RE.containsMatchIn(u)) {
        score += 10
    }

    // PNGs are neutral.
    return score
}

// Alt attribute usually means non-presentational image.
fun scoreAttr(img: Selection): Int {
    if (img.attr("alt") != null) {
        return 5
    }
    return 0
}

// Look through our parent and grandparent for figure-like
// container elements, give a bonus if we find them
fun scoreByParents(img: Selection): Int {
    var score = 0
    val raw = img.elements.firstOrNull() ?: return score
    val figParent = raw.parents().firstOrNull { it.tagName() == "figure" }

    if (figParent != null) {
        score += 25
    }

    val parent = raw.parent()
    val gParent = parent?.parent()

    listOfNotNull(parent, gParent).forEach { node ->
        if (PHOTO_HINTS_RE.containsMatchIn(getSig(img.doc.wrap(node)))) {
            score += 15
        }
    }

    return score
}

// Look at our immediate sibling and see if it looks like it's a
// caption. Bonus if so.
fun scoreBySibling(img: Selection): Int {
    var score = 0
    val raw = img.elements.firstOrNull() ?: return score
    val sibling = raw.nextElementSibling() ?: return score

    if (sibling.tagName().lowercase() == "figcaption") {
        score += 25
    }

    if (PHOTO_HINTS_RE.containsMatchIn(getSig(img.doc.wrap(sibling)))) {
        score += 15
    }

    return score
}

fun scoreByDimensions(img: Selection): Int {
    var score = 0

    val width = img.attr("width")?.toDoubleOrNull()
    val height = img.attr("height")?.toDoubleOrNull()
    val src = img.attr("src")

    // Penalty for skinny images
    if (width != null && width <= 50) {
        score -= 50
    }

    // Penalty for short images
    if (height != null && height <= 50) {
        score -= 50
    }

    if (width != null && height != null && src != null && !src.contains("sprite")) {
        val area = width * height
        score +=
            if (area < 5000) {
                // Smaller than 50 x 100
                -100
            } else {
                (area / 1000).roundToInt()
            }
    }

    return score
}

fun scoreByPosition(
    imgs: List<*>,
    index: Int,
): Int = imgs.size / 2 - index
