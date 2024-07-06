package com.jocmp.capyreader.ui.settings

import com.jocmp.capyreader.transfers.OPMLImportWorker
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val settingsModule = module {
    viewModel {
        SettingsViewModel(
            account = get(),
            accountManager = get(),
            refreshScheduler = get(),
            appPreferences = get(),
            application = get()
        )
    }
    worker { OPMLImportWorker(get(), get()) }
}
