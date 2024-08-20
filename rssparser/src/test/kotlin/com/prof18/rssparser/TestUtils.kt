package com.prof18.rssparser

import com.prof18.rssparser.internal.ParserInput
import java.io.File
import java.io.FileInputStream

internal fun readFileFromResources(
    resourceName: String
): ParserInput {
    val file = File("src/test/resources/$resourceName")
    return ParserInput(
        inputStream = FileInputStream(file)
    )
}

internal fun readFileFromResourcesAsString(
    resourceName: String
): String {
    val file = File("src/test/resources/$resourceName")
    return file.readText()
}
