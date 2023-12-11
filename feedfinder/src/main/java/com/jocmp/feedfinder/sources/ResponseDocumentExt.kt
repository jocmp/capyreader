package com.jocmp.feedfinder.sources

import com.jocmp.feedfinder.Response
import com.jocmp.feedfinder.parser.Parser
import org.jsoup.nodes.Document

internal suspend fun Response.findDocument(): Document? {
    val result = parse(validate = false)

    if (result is Parser.Result.HTMLDocument) {
        return result.document
    }

    return null
}
