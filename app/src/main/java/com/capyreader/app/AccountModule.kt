package com.capyreader.app

import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.notifications.NotificationHelper
import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capy.DatabaseProvider
import com.jocmp.capy.db.Database
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module

val accountModule = module {
    single<Database> {
        val accountID = runBlocking { get<AppPreferences>().accountID.get() }
        get<DatabaseProvider>().build(accountID = accountID)
    }
    single<Account> {
        runBlocking {
            get<AccountManager>().findByID(
                id = get<AppPreferences>().accountID.get(),
                database = get<Database>()
            )!!
        }
    }
    single<NotificationHelper> { NotificationHelper(account = get(), applicationContext = get()) }
}
