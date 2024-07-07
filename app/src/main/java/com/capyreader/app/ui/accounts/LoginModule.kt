package com.capyreader.app.ui.accounts

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {
    viewModel {
        AddAccountViewModel(
            accountManager = get(),
            appPreferences = get()
        )
    }
    viewModel { parameters ->
        LoginViewModel(
            account = parameters.getOrNull(),
            accountManager = get(),
            appPreferences = get()
        )
    }
}
