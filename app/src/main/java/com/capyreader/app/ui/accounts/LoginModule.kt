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
    viewModel {
        LoginViewModel(
            handle = get(),
            accountManager = get(),
            appPreferences = get(),
            clientCertManager = get(),
        )
    }
    viewModel {
        UpdateLoginViewModel(
            account = get(),
            clientCertManager = get(),
        )
    }
}
