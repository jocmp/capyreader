package com.jocmp.basilreader.ui.articles

import com.jocmp.basil.Account
import com.jocmp.basilreader.common.AppPreferences
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

internal val articlesModule = module {
    factory {
        AddFeedStateHolder(
            account = get<Account>(parameters = { parametersOf(get<AppPreferences>().accountID.get()) }),
        )
    }
    viewModel {
        val appPreferences = get<AppPreferences>()

        AccountViewModel(
            account = get<Account>(parameters = { parametersOf(appPreferences.accountID.get()) }),
            refreshScheduler = get(),
            appPreferences = appPreferences
        )
    }
    viewModel {
        EditFeedViewModel(
            savedStateHandle = get(),
            accountManager = get(),
            appPreferences = get()
        )
    }
}
