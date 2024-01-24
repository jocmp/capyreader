package com.jocmp.feedfinder

import com.jocmp.feedfinder.parser.Feed
import com.jocmp.feedfinder.parser.Parser
import java.net.URL

internal class Response(val url: URL, val body: String) {
    suspend fun parse(validate: Boolean = true): Parser.Result {
        if (parsed == null) {
            parsed = Parser.parse(body, url = url, validate = validate)
        }

        return parsed!!
    }

    private var parsed: Parser.Result? = null
}

internal suspend fun Response.toParsedFeed(): Feed? {
    return (parse() as? Parser.Result.ParsedFeed)?.feed
}
