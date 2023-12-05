package com.jocmp.feedfinder

import com.jocmp.feedfinder.parser.FakeFeed
import com.jocmp.feedfinder.parser.Feed

class Response(val body: String?) {
    suspend fun parse(): Feed {
        return FakeFeed()
    }
}
