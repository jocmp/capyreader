package com.jocmp.basil

import android.print.PrintAttributes.Margins
import javax.crypto.Mac

data class MacroProcessor(
    val template: String,
    val substitutions: Map<String, String>,
    val openTag: String = OPEN_TAG,
    val closeTag: String = CLOSE_TAG,
) {
    val renderedText: String by lazy { process() }

    companion object {
        const val OPEN_TAG = "{{"
        const val CLOSE_TAG = "}}"
    }
}

private fun MacroProcessor.process(): String {
    val queue = ArrayDeque<Char>()
    var result = ""

    template.forEach { token ->
        if (openTag.contains(token)) {
            queue.add(token)
        } else if (queue.isNotEmpty() && closeTag.contains(token)) {
            queue.add(token)
        } else if (startsWithTag(queue)) {
            queue.add(token)
        }

        if (queue.isEmpty()) {
            result += token
        } else if (hasEvenTags(queue)) {
            var taggedKey = ""

            while (queue.isNotEmpty()) {
                taggedKey += queue.removeFirst()
            }

            val key = extractKey(taggedKey)

            result += substitutions.getOrDefault(key, taggedKey)
        } else if (hasHangingOpenTag(queue, token)) {
            while (queue.isNotEmpty()) {
                result += queue.removeFirst()
            }

            result += token
        } else if (startedNewOpenTag(queue, token)) {
            while (queue.size > 1) {
                result += queue.removeFirst()
            }
        }
    }

    return result
}

private fun MacroProcessor.extractKey(taggedKey: String): String {
    return taggedKey.substring(
        openTag.length, taggedKey.length - closeTag.length
    )
}

private fun MacroProcessor.startsWithTag(queue: ArrayDeque<Char>): Boolean {
    return queue.take(openTag.length).joinToString("") == openTag
}

private fun MacroProcessor.endsWithTag(queue: ArrayDeque<Char>): Boolean {
    return queue.takeLast(closeTag.length).joinToString("") == closeTag
}

private fun MacroProcessor.hasEvenTags(queue: ArrayDeque<Char>): Boolean {
    val tagSum = openTag.length + closeTag.length

    return queue.size > tagSum && startsWithTag(queue) && endsWithTag(queue)
}

private fun MacroProcessor.hasHangingOpenTag(queue: ArrayDeque<Char>, currentToken: Char): Boolean {
    return queue.size >= 1 &&
            queue.size <= openTag.length &&
            !openTag.contains(currentToken)
}

private fun MacroProcessor.startedNewOpenTag(queue: ArrayDeque<Char>, currentToken: Char): Boolean {
    return queue.size > openTag.length &&
            openTag.contains(currentToken)
}
