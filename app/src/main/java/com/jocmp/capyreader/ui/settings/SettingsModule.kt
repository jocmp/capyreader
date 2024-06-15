package com.jocmp.capyreader.ui.settings

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel {
        SettingsViewModel(
            account = get(),
            accountManager = get(),
            refreshScheduler = get(),
            appPreferences = get()
        )
    }
}
