package com.jocmp.rssparser

import com.jocmp.rssparser.internal.ParserInput
import java.io.File

internal fun readFileFromResources(
    resourceName: String
): ParserInput {
    val file = File("src/test/resources/$resourceName")

    return ParserInput(file.readBytes(), charset = null)
}
