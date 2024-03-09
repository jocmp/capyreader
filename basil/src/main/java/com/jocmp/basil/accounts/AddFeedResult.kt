package com.jocmp.basil.accounts

import com.jocmp.basil.Feed

sealed class AddFeedResult {
    data class Success(val feed: Feed): AddFeedResult()

    data class MultipleChoices(val choices: List<FeedOption>): AddFeedResult()
}

data class FeedOption(
    val feedURL: String,
    val title: String
)
