package com.jocmp.basilreader.refresher

import com.jocmp.basil.AccountManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedRefresher(private val accountManager: AccountManager) {
    suspend fun refresh() {
        return withContext(Dispatchers.IO) {
            accountManager.accounts.forEach { account ->
                account.refreshAll()
            }
        }
    }
}
