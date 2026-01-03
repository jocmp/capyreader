package com.capyreader.app.ui.articles

import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.articles.feeds.edit.EditFeedViewModel
import com.jocmp.capy.articles.ArticleRenderer
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val articlesModule = module {
    factory {
        AddFeedViewModel(
            account = get(),
            appPreferences = get()
        )
    }
    single {
        ArticleRenderer(
            context = get(),
            textSize = get<AppPreferences>().readerOptions.fontSize,
            fontOption = get<AppPreferences>().readerOptions.fontFamily,
            titleFontSize = get<AppPreferences>().readerOptions.titleFontSize,
            textAlignment = get<AppPreferences>().readerOptions.titleTextAlignment,
            titleFollowsBodyFont = get<AppPreferences>().readerOptions.titleFollowsBodyFont,
            hideTopMargin = get<AppPreferences>().readerOptions.pinTopToolbar,
            enableHorizontalScroll = get<AppPreferences>().readerOptions.enableHorizontaPagination,
        )
    }
    viewModel {
        val appPreferences = get<AppPreferences>()

        ArticleScreenViewModel(
            account = get(),
            appPreferences = appPreferences,
            notificationHelper = get(),
            application = get(),
            articleTranslator = get(),
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
