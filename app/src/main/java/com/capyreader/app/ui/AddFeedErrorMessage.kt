package com.capyreader.app.ui

import com.capyreader.app.R
import com.jocmp.capy.accounts.AddFeedResult

val AddFeedResult.Error.translationKey: Int
    get() = when (this) {
        is AddFeedResult.Error.FeedNotFound -> R.string.add_feed_feed_not_error
        is AddFeedResult.Error.ConnectionError -> R.string.add_feed_network_error
        is AddFeedResult.Error.NetworkError -> R.string.add_feed_network_error
        is AddFeedResult.Error.SaveFailure -> R.string.add_feed_save_error
    }
