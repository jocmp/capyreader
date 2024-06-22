package com.jocmp.capyreader.ui.login

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {
    viewModel { parameters ->
        LoginViewModel(
            account = parameters.getOrNull(),
            accountManager = get(),
            appPreferences = get()
        )
    }
}
