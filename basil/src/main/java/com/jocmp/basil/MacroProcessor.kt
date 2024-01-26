package com.jocmp.basil

data class MacroProcessor(
    val template: String,
    val substitutions: Map<String, String>,
    val openTag: String = OPEN_TAG,
    val closeTag: String = CLOSE_TAG,
) {
    val renderedText: String by lazy { process() }

    companion object {
        const val OPEN_TAG = "[["
        const val CLOSE_TAG = "]]"
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
        } else if (queue.size > 1 && endsWithTag(queue)) {
            var taggedKey = ""

            while (queue.isNotEmpty()) {
                taggedKey += queue.removeFirst()
            }

            val key = extractKey(taggedKey)

            result += substitutions.getOrDefault(key, taggedKey)
        }
    }

    return result
}

private fun MacroProcessor.extractKey(taggedKey: String): String {
    return taggedKey.substring(
        openTag.length,
        taggedKey.length - closeTag.length
    )
}

private fun MacroProcessor.startsWithTag(queue: ArrayDeque<Char>): Boolean {
    return queue.take(openTag.length).joinToString("") == openTag
}

private fun MacroProcessor.endsWithTag(queue: ArrayDeque<Char>): Boolean {
    return queue.takeLast(closeTag.length).joinToString("") == closeTag
}
