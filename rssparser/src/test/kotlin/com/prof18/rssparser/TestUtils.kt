package com.prof18.rssparser

import com.prof18.rssparser.internal.ParserInput
import java.io.File

internal fun readFileFromResources(
    resourceName: String
): ParserInput {
    val file = File("src/test/resources/$resourceName")

    return ParserInput(file.readBytes())
}
