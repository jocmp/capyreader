package com.jocmp.capy.accounts

import com.jocmp.capy.Feed

sealed class AddFeedResult {
    enum class ErrorType {
        FEED_NOT_FOUND,
        NETWORK_ERROR,
        SAVE_FAILURE,
    }

    sealed class AddFeedError: Exception() {
        class FeedNotFound: AddFeedError()
        class NetworkError: AddFeedError()

        class SaveFailure: AddFeedError()
    }

    data class Success(val feed: Feed): AddFeedResult()

    data class MultipleChoices(val choices: List<FeedOption>): AddFeedResult()

    data class Failure(val error: AddFeedError): AddFeedResult()
}

data class FeedOption(
    val feedURL: String,
    val title: String
)
