package com.jocmp.feedfinder.parser

internal object Parser {
    class NoFeedFoundError: Throwable()

    // Parse as XML
    //   return result if feed is valid
    // if result is not valid, attempt to detect encoding
    // if encoding is present and encoding detection confidence is high,
    //   reparse XML
    //   return result if feed is valid
    // if result is not present, parse as JSON
    //   return result if feed is valid
    // if no result, raise a NotFeed error

    // Parser
    // - XMLFeed
    // - JSONFeed
    // - HTML
    suspend fun parse(body: String): Feed {
        val xmlFeed = XMLFeed.from(body)

        if (xmlFeed.isValid()) {
            return xmlFeed
        }

        throw NoFeedFoundError()
    }

//    sealed class Document {
//        class XMLDocument
//        class HTMLDocument
//        class JSONDocument
//    }
}
