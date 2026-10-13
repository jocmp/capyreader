package com.capyreader.app.ui.articles

import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.addintent.AddLinkViewModel
import com.capyreader.app.ui.articles.audio.AudioPlayerController
import com.capyreader.app.ui.articles.feeds.edit.EditFeedViewModel
import com.capyreader.app.ui.articles.reader.VideoAspectRatios
import com.capyreader.app.ui.articles.reader.VideoPlayerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val articlesModule = module {
    single { ArticleSessionCutoff() }
    factory {
        AddFeedViewModel(
            account = get(),
        )
    }
    factory {
        AddLinkViewModel(
            account = get(),
        )
    }
    single {
        VideoAspectRatios(
            context = get()
        )
    }
    single {
        AudioPlayerController(
            context = get()
        )
    }
    viewModel {
        val appPreferences = get<AppPreferences>()

        ArticleScreenViewModel(
            account = get(),
            appPreferences = appPreferences,
            notificationHelper = get(),
            application = get(),
            articleCutoff = get(),
        )
    }
    viewModel {
        ArticleViewModel(
            account = get(),
            appPreferences = get(),
            application = get(),
            notificationHelper = get(),
            articleCutoff = get(),
        )
    }
    viewModel {
        VideoPlayerViewModel(
            application = get(),
        )
    }
    viewModel {
        EditFeedViewModel(
            account = get(),
        )
    }
    viewModel {
        EditFolderViewModel(
            account = get(),
            appPreferences = get(),
        )
    }
}
