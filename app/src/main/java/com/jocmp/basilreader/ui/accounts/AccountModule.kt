package com.jocmp.basilreader.ui.accounts

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
            refreshScheduler = get(),
        )
    }
}
