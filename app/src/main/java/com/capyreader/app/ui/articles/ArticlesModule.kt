package com.capyreader.app.ui.articles

import android.content.Context
import com.capyreader.app.R
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.articles.audio.AudioPlayerController
import com.capyreader.app.ui.articles.feeds.edit.EditFeedViewModel
import com.jocmp.capy.articles.ArticleRenderer
import com.jocmp.capy.articles.AudioPlayerLabels
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
        AudioPlayerController(
            context = get()
        )
    }
    single {
        val context = get<Context>()

        ArticleRenderer(
            context = context,
            textSize = get<AppPreferences>().readerOptions.fontSize,
            fontOption = get<AppPreferences>().readerOptions.fontFamily,
            titleFontSize = get<AppPreferences>().readerOptions.titleFontSize,
            textAlignment = get<AppPreferences>().readerOptions.titleTextAlignment,
            titleFollowsBodyFont = get<AppPreferences>().readerOptions.titleFollowsBodyFont,
            hideTopMargin = get<AppPreferences>().readerOptions.pinTopToolbar,
            enableHorizontalScroll = get<AppPreferences>().readerOptions.enableHorizontaPagination,
            audioPlayerLabels = AudioPlayerLabels(
                play = context.getString(R.string.audio_player_play),
                pause = context.getString(R.string.audio_player_pause),
            ),
        )
    }
    viewModel {
        val appPreferences = get<AppPreferences>()

        ArticleScreenViewModel(
            account = get(),
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
