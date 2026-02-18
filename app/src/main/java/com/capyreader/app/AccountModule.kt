package com.capyreader.app

import com.capyreader.app.notifications.NotificationHelper
import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capy.DatabaseProvider
import com.jocmp.capy.db.Database
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module

fun accountModule(accountID: String) = module {
    single<Database> {
        get<DatabaseProvider>().build(accountID = accountID)
    }
    single<Account> {
        runBlocking {
            get<AccountManager>().findByID(
                id = accountID,
                database = get<Database>()
            )!!
        }
    }
    single<NotificationHelper> { NotificationHelper(account = get(), applicationContext = get()) }
}
