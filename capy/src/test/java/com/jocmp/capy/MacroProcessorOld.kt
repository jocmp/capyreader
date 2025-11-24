package com.jocmp.capy

// Old unoptimized implementation for benchmarking comparison
data class MacroProcessorOld(
    val template: String,
    val substitutions: Map<String, String>,
    val openTag: String = "{{",
    val closeTag: String = "}}",
) {
    val renderedText: String by lazy { process() }
}

private fun MacroProcessorOld.process(): String {
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

private fun MacroProcessorOld.extractKey(taggedKey: String): String {
    return taggedKey.substring(
        openTag.length, taggedKey.length - closeTag.length
    )
}

private fun MacroProcessorOld.startsWithTag(queue: ArrayDeque<Char>): Boolean {
    return queue.take(openTag.length).joinToString("") == openTag
}

private fun MacroProcessorOld.endsWithTag(queue: ArrayDeque<Char>): Boolean {
    return queue.takeLast(closeTag.length).joinToString("") == closeTag
}

private fun MacroProcessorOld.hasEvenTags(queue: ArrayDeque<Char>): Boolean {
    val tagSum = openTag.length + closeTag.length

    return queue.size > tagSum && startsWithTag(queue) && endsWithTag(queue)
}

private fun MacroProcessorOld.hasHangingOpenTag(queue: ArrayDeque<Char>, currentToken: Char): Boolean {
    return queue.size >= 1 &&
            queue.size <= openTag.length &&
            !openTag.contains(currentToken)
}

private fun MacroProcessorOld.startedNewOpenTag(queue: ArrayDeque<Char>, currentToken: Char): Boolean {
    return queue.size > openTag.length &&
            openTag.contains(currentToken)
}
