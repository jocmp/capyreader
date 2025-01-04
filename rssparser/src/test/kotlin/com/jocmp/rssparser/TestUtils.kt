package com.jocmp.rssparser

import com.jocmp.rssparser.internal.ParserInput
import java.io.File
import java.nio.charset.Charset

internal fun readFileFromResources(
    resourceName: String,
    charset: Charset? = null,
): ParserInput {
    val file = File("src/test/resources/$resourceName")

    return ParserInput(file.readBytes(), charset = charset)
}
