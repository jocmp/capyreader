package com.capyreader.app.refresher

import com.jocmp.capy.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedRefresher(
    private val account: Account,
) {
    suspend fun refresh() {
        return withContext(Dispatchers.IO) {
            account.refresh()
        }
    }
}
