package com.jocmp.basil.accounts

import com.jocmp.basil.Feed

sealed class AddFeedResult {
    data class Success(val feedTitle: String): AddFeedResult()

    data class MultipleChoices(val choices: List<FeedChoice>): AddFeedResult()
}

data class FeedChoice(
    val feedURL: String,
    val title: String
)
