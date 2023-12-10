package com.jocmp.feedfinder

import com.jocmp.feedfinder.parser.FakeFeed
import com.jocmp.feedfinder.parser.Feed
import com.jocmp.feedfinder.parser.Parser
import com.jocmp.feedfinder.parser.XMLFeed

internal class Response(val body: String) {
    suspend fun parse(validate: Boolean = false): Parser.Result {
        if (parsed == null) {
            parsed = Parser.parse(body, validate = validate)
        }

        return parsed!!
    }

    private var parsed: Parser.Result? = null
}
