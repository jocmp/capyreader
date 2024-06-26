package com.jocmp.capyreader

import com.jocmp.capy.AccountManager
import com.jocmp.capy.DatabaseProvider
import com.jocmp.capy.PreferenceStoreProvider
import com.jocmp.capyreader.common.AndroidDatabaseProvider
import com.jocmp.capyreader.common.AppPreferences
import com.jocmp.capyreader.common.EncryptedPreferenceStoreProvider
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
