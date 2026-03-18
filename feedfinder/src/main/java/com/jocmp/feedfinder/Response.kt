package com.jocmp.feedfinder

import com.jocmp.feedfinder.parser.Feed
import com.jocmp.feedfinder.parser.Parser
import java.net.URL
import java.nio.charset.Charset

internal class Response(
    val url: URL,
    val body: String,
    val charset: Charset?,
    val headers: Map<String, List<String>> = emptyMap(),
) {
    suspend fun parse(validate: Boolean = true): Parser.Result {
        if (parsed == null) {
            parsed = Parser.parse(body, url = url, charset = charset, validate = validate)
        }

        return parsed!!
    }

    private var parsed: Parser.Result? = null
}

internal suspend fun Response.toParsedFeed(): Feed? {
    return (parse() as? Parser.Result.ParsedFeed)?.feed
}
