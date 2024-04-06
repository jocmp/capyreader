package com.jocmp.basilreader.refresher

import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.common.AppPreferences
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
