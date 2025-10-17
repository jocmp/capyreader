package com.capyreader.app

import com.capyreader.app.common.AndroidClientCertManager
import com.capyreader.app.common.AndroidDatabaseProvider
import com.capyreader.app.common.AppFaviconFetcher
import com.capyreader.app.common.SharedPreferenceStoreProvider
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.AccountManager
import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.DatabaseProvider
import com.jocmp.capy.PreferenceStoreProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val common = module {
    single<PreferenceStoreProvider> { SharedPreferenceStoreProvider(get()) }
    single<DatabaseProvider> { AndroidDatabaseProvider(context = get()) }
    single<ClientCertManager> { AndroidClientCertManager(context = get()) }
    single {
        AccountManager(
            rootFolder = androidContext().filesDir.toURI(),
            databaseProvider = get(),
            cacheDirectory = androidContext().cacheDir.toURI(),
            preferenceStoreProvider = get(),
            faviconFetcher = AppFaviconFetcher(get()),
            clientCertManager = get(),
        )
    }
    single { AppPreferences(get()) }
}
