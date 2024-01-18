package com.jocmp.basilreader.ui.accounts

import com.jocmp.basilreader.settings
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val accountModule = module {
    viewModel {
        AccountIndexViewModel(
            accountManager = get(),
            androidContext().settings
        )
    }
    viewModel {
        AccountSettingsViewModel(
            savedStateHandle = get(),
            accountManager = get(),
            settings = androidContext().settings
        )
    }
}
