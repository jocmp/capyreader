package com.jocmp.basilreader.ui.articles

import com.jocmp.basilreader.settings
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val articlesModule = module {
    viewModel {
        AccountViewModel(
            accountManager = get(),
            settings = androidContext().settings
        )
    }
    viewModel {
        EditFeedViewModel(
            savedStateHandle = get(),
            accountManager = get(),
            settings = androidContext().settings
        )
    }
    viewModel {
        EditFolderViewModel(
            savedStateHandle = get(),
            accountManager = get(),
            settings = androidContext().settings
        )
    }
}
