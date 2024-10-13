package com.capyreader.app.ui.settings

import com.capyreader.app.transfers.OPMLImportWorker
import com.capyreader.app.ui.settings.panels.AccountSettingsViewModel
import com.capyreader.app.ui.settings.panels.DisplaySettingsViewModel
import com.capyreader.app.ui.settings.panels.GeneralSettingsViewModel
import com.capyreader.app.ui.settings.panels.GesturesSettingsViewModel
import com.capyreader.app.ui.settings.panels.NotificationSettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val settingsModule = module {
    viewModel {
        GeneralSettingsViewModel(
            refreshScheduler = get(),
            account = get(),
            appPreferences = get()
        )
    }
    viewModel {
        AccountSettingsViewModel(
            account = get(),
            accountManager = get(),
            appPreferences = get(),
            application = get()
        )
    }
    viewModel {
        DisplaySettingsViewModel(
            account = get(),
            appPreferences = get(),
        )
    }
    viewModel {
        GesturesSettingsViewModel(
            appPreferences = get(),
        )
    }
    viewModel {
       NotificationSettingsViewModel(
           account = get()
       )
    }
    worker { OPMLImportWorker(get(), get()) }
}
