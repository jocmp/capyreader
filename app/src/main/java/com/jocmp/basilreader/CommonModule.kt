package com.jocmp.basilreader

import com.jocmp.basil.AccountManager
import com.jocmp.basil.DatabaseProvider
import com.jocmp.basil.PreferencesProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val commonModule = module {
    single<DatabaseProvider> { AndroidDatabaseProvider(get()) }
    single { AccountPreferencesSerializer() }
    single<PreferencesProvider> {
        AccountPreferencesProvider(
            serializer = get(),
            context = get()
        )
    }
    single {
        AccountManager(
            rootFolder = androidContext().filesDir.toURI(),
            databaseProvider = get(),
            preferencesProvider = get()
        )
    }
}
