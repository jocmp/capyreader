package com.jocmp.capy.accounts

import com.jocmp.capy.Feed

sealed class AddFeedResult {
    sealed class AddFeedError : Exception() {
        class FeedNotFound : AddFeedError()
        class NetworkError : AddFeedError()
        class SaveFailure : AddFeedError()
    }

    data class Success(val feed: Feed) : AddFeedResult()

    data class MultipleChoices(val choices: List<FeedOption>) : AddFeedResult()

    data class Failure(val error: AddFeedError) : AddFeedResult()

    companion object {
        fun networkError() = Failure(AddFeedError.NetworkError())

        fun feedNotFound() = Failure(AddFeedError.FeedNotFound())

        fun saveFailure() = Failure(AddFeedError.SaveFailure())
    }
}

data class FeedOption(
    val feedURL: String,
    val title: String
)
