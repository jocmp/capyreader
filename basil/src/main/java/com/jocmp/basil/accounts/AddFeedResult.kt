package com.jocmp.basil.accounts

sealed class AddFeedResult {
    data class Success(val feedTitle: String): AddFeedResult()

    data class MultipleChoices(val choices: List<FeedOption>): AddFeedResult()
}

data class FeedOption(
    val feedURL: String,
    val title: String
)
