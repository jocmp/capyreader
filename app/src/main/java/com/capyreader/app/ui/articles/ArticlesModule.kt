package com.capyreader.app.ui.articles

import com.jocmp.capy.Account
import com.capyreader.app.common.AppPreferences
import com.jocmp.capy.articles.ArticleRenderer
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
    single {
        ArticleRenderer(
            context = get(),
            textSize = get<AppPreferences>().textSize,
            fontOption = get<AppPreferences>().fontOption,
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
}
