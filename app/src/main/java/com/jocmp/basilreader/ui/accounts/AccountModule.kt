package com.jocmp.basilreader.ui.accounts

import com.jocmp.basil.FeedSearch
import com.jocmp.feedfinder.DefaultFeedFinder
import com.jocmp.feedfinder.FeedFinder
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val accountModule = module {
    viewModel {
        AccountIndexViewModel(
            accountManager = get(),
            appPreferences = get()
        )
    }
    viewModel {
        AccountSettingsViewModel(
            savedStateHandle = get(),
            accountManager = get(),
        )
    }
}
