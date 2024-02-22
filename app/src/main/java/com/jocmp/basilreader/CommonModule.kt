package com.jocmp.basilreader

import com.jocmp.basil.AccountManager
import com.jocmp.basil.DatabaseProvider
import com.jocmp.basil.PreferenceStoreProvider
import com.jocmp.basilreader.common.AndroidDatabaseProvider
import com.jocmp.basilreader.common.EncryptedPreferenceStoreProvider
import com.jocmp.basilreader.common.AppPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val common = module {
    single<DatabaseProvider> { AndroidDatabaseProvider(get()) }
    single<PreferenceStoreProvider> { EncryptedPreferenceStoreProvider(get()) }
    single { AppPreferences(get()) }
    single {
        AccountManager(
            rootFolder = androidContext().filesDir.toURI(),
            databaseProvider = get(),
            preferenceStoreProvider = get(),
        )
    }
}
