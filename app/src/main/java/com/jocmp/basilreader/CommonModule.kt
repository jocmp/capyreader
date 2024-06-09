package com.jocmp.basilreader

import com.jocmp.basil.AccountManager
import com.jocmp.basil.DatabaseProvider
import com.jocmp.basil.PreferenceStoreProvider
import com.jocmp.basilreader.common.AndroidDatabaseProvider
import com.jocmp.basilreader.common.AppPreferences
import com.jocmp.basilreader.common.EncryptedPreferenceStoreProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val common = module {
    single<PreferenceStoreProvider> { EncryptedPreferenceStoreProvider(get()) }
    single<DatabaseProvider> { AndroidDatabaseProvider(context = get()) }
    single {
        AccountManager(
            rootFolder = androidContext().filesDir.toURI(),
            databaseProvider = get(),
            preferenceStoreProvider = get()
        )
    }
    single { AppPreferences(get()) }
}
