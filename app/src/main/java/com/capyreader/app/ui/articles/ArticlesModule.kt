package com.capyreader.app.ui.articles

import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.Account
import com.jocmp.capy.articles.ArticleRenderer
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

internal val articlesModule = module {
    factory {
        AddFeedViewModel(
            account = get<Account>(parameters = { parametersOf(get<AppPreferences>().accountID.get()) }),
            appPreferences = get()
        )
    }
    single {
        ArticleRenderer(
            context = get(),
            textSize = get<AppPreferences>().readerOptions.fontSize,
            fontOption = get<AppPreferences>().readerOptions.fontFamily,
            hideTopMargin = get<AppPreferences>().readerOptions.pinToolbars,
            enableHorizontalScroll = get<AppPreferences>().readerOptions.enableHorizontaPagination,
        )
    }
    viewModel {
        val appPreferences = get<AppPreferences>()

        ArticleScreenViewModel(
            account = get<Account>(parameters = { parametersOf(appPreferences.accountID.get()) }),
            appPreferences = appPreferences,
            notificationHelper = get(),
            application = get(),
        )
    }
    viewModel {
        EditFeedViewModel(
            account = get(),
            appPreferences = get()
        )
    }
    viewModel {
        EditFolderViewModel(
            account = get(),
            appPreferences = get()
        )
    }
}
