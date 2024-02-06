package com.jocmp.basilreader

import com.jocmp.basil.AccountManager
import com.jocmp.basil.DatabaseProvider
import com.jocmp.basil.PreferenceStoreProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val common = module {
    single { buildOkHttpClient(get()) }
    single<DatabaseProvider> { AndroidDatabaseProvider(get()) }
    single<PreferenceStoreProvider> { AndroidPreferenceStoreProvider(get()) }
    single { AppPreferences(get()) }
    single {
        AccountManager(
            rootFolder = androidContext().filesDir.toURI(),
            databaseProvider = get(),
            preferenceStoreProvider = get(),
            httpClient = get(),
        )
    }
}
