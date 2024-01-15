package com.jocmp.basilreader.ui.accounts

import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.AccountPreferencesSerializer
import com.jocmp.basil.DatabaseProvider
import com.jocmp.basilreader.AndroidDatabaseProvider
import com.jocmp.basilreader.AccountPreferencesProvider
import com.jocmp.basil.PreferencesProvider
import com.jocmp.basilreader.settings
import com.jocmp.basilreader.ui.articles.EditFeedViewModel
import com.jocmp.basilreader.ui.articles.EditFolderScreen
import com.jocmp.basilreader.ui.articles.EditFolderViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val accountModule = module {
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
    viewModel {
        AccountViewModel(
            accountManager = get(),
            settings = androidContext().settings
        )
    }
    viewModel {
        EditFeedViewModel(
            savedStateHandle = get(),
            accountManager = get(),
            settings = androidContext().settings
        )
    }
    viewModel {
        EditFolderViewModel(
            savedStateHandle = get(),
            accountManager = get(),
            settings = androidContext().settings
        )
    }
}
