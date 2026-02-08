package com.jocmp.capy.accounts

import com.jocmp.capy.Feed

sealed class AddFeedResult {
    sealed class Error : Throwable() {
        class BlockedBySite : Error()
        class ConnectionError : Error()
        class FeedNotFound : Error()
        class NetworkError : Error()
        class SaveFailure : Error()
    }

    data class Success(val feed: Feed) : AddFeedResult()

    data class MultipleChoices(val choices: List<FeedOption>) : AddFeedResult()

    data class Failure(val error: Error) : AddFeedResult()

    companion object {
        fun blockedBySite() = Failure(Error.BlockedBySite())

        fun connectionError() = Failure(Error.ConnectionError())

        fun networkError() = Failure(Error.NetworkError())

        fun feedNotFound() = Failure(Error.FeedNotFound())

        fun saveFailure() = Failure(Error.SaveFailure())
    }
}

data class FeedOption(
    val feedURL: String,
    val title: String
)
