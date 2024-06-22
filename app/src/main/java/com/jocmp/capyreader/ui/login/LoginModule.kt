package com.jocmp.capyreader.ui.login

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {
    viewModel {
        LoginViewModel(
            accountManager = get(),
            appPreferences = get()
        )
    }
}
