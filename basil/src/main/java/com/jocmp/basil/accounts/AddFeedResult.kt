package com.jocmp.basil.accounts

import com.jocmp.basil.Feed

sealed class AddFeedResult {
    enum class ErrorType {
        FEED_NOT_FOUND,
        NETWORK_ERROR,
        SAVE_FAILURE,
    }

    data class Success(val feed: Feed): AddFeedResult()

    data class MultipleChoices(val choices: List<FeedOption>): AddFeedResult()

    data class Failure(val error: ErrorType): AddFeedResult()
}

data class FeedOption(
    val feedURL: String,
    val title: String
)
