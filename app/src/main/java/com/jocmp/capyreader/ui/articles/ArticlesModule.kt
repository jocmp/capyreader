package com.jocmp.capyreader.ui.articles

import com.jocmp.capy.Account
import com.jocmp.capyreader.common.AppPreferences
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

internal val articlesModule = module {
    factory {
        AddFeedStateHolder(
            account = get<Account>(parameters = { parametersOf(get<AppPreferences>().accountID.get()) }),
        )
    }
    factory {
        AddFeedStateHolder(
            account = get<Account>(parameters = { parametersOf(get<AppPreferences>().accountID.get()) }),
        )
    }
    viewModel {
        val appPreferences = get<AppPreferences>()

        ArticleScreenViewModel(
            account = get<Account>(parameters = { parametersOf(appPreferences.accountID.get()) }),
            appPreferences = appPreferences,
            application = get()
        )
    }
    viewModel {
        EditFeedViewModel(
            account = get(),
            appPreferences = get()
        )
    }
    viewModel {
        UpdateAuthViewModel(get())
    }
}
