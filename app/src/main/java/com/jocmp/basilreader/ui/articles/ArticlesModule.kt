package com.jocmp.basilreader.ui.articles

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val articlesModule = module {
    viewModel {
        AddFeedViewModel(
            accountManager = get(),
            appPreferences = get(),
        )
    }
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
