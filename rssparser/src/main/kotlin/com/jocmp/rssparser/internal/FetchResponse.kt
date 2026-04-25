package com.jocmp.rssparser.internal

import com.jocmp.rssparser.model.ConditionalGetInfo

internal data class FetchResponse(
    val parserInput: ParserInput?,
    val conditionalGet: ConditionalGetInfo,
) {
    val notModified: Boolean
        get() = parserInput == null
}
