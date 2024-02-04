package com.jocmp.basilreader.ui.articles

import com.jocmp.basil.FeedSearch
import com.jocmp.feedfinder.DefaultFeedFinder
import com.jocmp.feedfinder.FeedFinder
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.get
import org.koin.dsl.module

internal val articlesModule = module {
    single<FeedFinder> { DefaultFeedFinder() }
    single { FeedSearch(get()) }
    viewModel {
        AccountViewModel(
            accountManager = get(),
            appPreferences = get(),
        )
    }
    viewModel {
        EditFeedViewModel(
            savedStateHandle = get(),
            accountManager = get(),
            appPreferences = get()
        )
    }
    viewModel {
        EditFolderViewModel(
            savedStateHandle = get(),
            accountManager = get(),
            appPreferences = get()
        )
    }
}
