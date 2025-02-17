package com.capyreader.app

import com.capyreader.app.common.AndroidDatabaseProvider
import com.capyreader.app.common.AppFaviconFetcher
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.common.SharedPreferenceStoreProvider
import com.jocmp.capy.AccountManager
import com.jocmp.capy.DatabaseProvider
import com.jocmp.capy.PreferenceStoreProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val common = module {
    single<PreferenceStoreProvider> { SharedPreferenceStoreProvider(get()) }
    single<DatabaseProvider> { AndroidDatabaseProvider(context = get()) }
    single {
        AccountManager(
            rootFolder = androidContext().filesDir.toURI(),
            databaseProvider = get(),
            cacheDirectory = androidContext().cacheDir.toURI(),
            preferenceStoreProvider = get(),
            faviconFetcher = AppFaviconFetcher(get())
        )
    }
    single { AppPreferences(get()) }
}
