package com.jocmp.feedfinder

enum class FeedError {
    IO_FAILURE,
    INVALID_URL,
    NO_FEEDS_FOUND,
    BLOCKED_BY_SITE,
}

val FeedError.asException: FeedException
    get() = FeedException(this)

class FeedException(val feedError: FeedError) : Exception(feedError.name)
