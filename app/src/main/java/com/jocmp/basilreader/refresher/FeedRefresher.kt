package com.jocmp.basilreader.refresher

import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.common.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedRefresher(
    private val accountManager: AccountManager,
    private val appPreferences: AppPreferences,
) {
    suspend fun refresh() {
        return withContext(Dispatchers.IO) {
            val account = accountManager
                .findByID(appPreferences.accountID.get()) ?: return@withContext

            account.refreshAll()
        }
    }
}
