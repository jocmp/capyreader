package com.jocmp.feedfinder

import java.net.URL

sealed class Result {
    class Success(val title: String, val feedURL: URL) : Result()

    class Failure(val error: FeedError) : Result()
}
