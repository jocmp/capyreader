package com.jocmp.capy

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
    val result = StringBuilder()

    template.forEach { token ->
        if (openTag.contains(token)) {
            queue.add(token)
        } else if (queue.isNotEmpty() && closeTag.contains(token)) {
            queue.add(token)
        } else if (startsWithTag(queue)) {
            queue.add(token)
        }

        if (queue.isEmpty()) {
            result.append(token)
        } else if (hasEvenTags(queue)) {
            val taggedKey = buildString(queue.size) {
                while (queue.isNotEmpty()) {
                    append(queue.removeFirst())
                }
            }

            val key = extractKey(taggedKey)

            result.append(substitutions.getOrDefault(key, taggedKey))
        } else if (hasHangingOpenTag(queue, token)) {
            while (queue.isNotEmpty()) {
                result.append(queue.removeFirst())
            }

            result.append(token)
        } else if (startedNewOpenTag(queue, token)) {
            while (queue.size > 1) {
                result.append(queue.removeFirst())
            }
        }
    }

    return result.toString()
}

private fun MacroProcessor.extractKey(taggedKey: String): String {
    return taggedKey.substring(
        openTag.length, taggedKey.length - closeTag.length
    )
}

private fun MacroProcessor.startsWithTag(queue: ArrayDeque<Char>): Boolean {
    if (queue.size < openTag.length) return false
    return (0 until openTag.length).all { i -> queue[i] == openTag[i] }
}

private fun MacroProcessor.endsWithTag(queue: ArrayDeque<Char>): Boolean {
    if (queue.size < closeTag.length) return false
    val offset = queue.size - closeTag.length
    return (0 until closeTag.length).all { i -> queue[offset + i] == closeTag[i] }
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
