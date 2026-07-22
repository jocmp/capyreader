package com.capyreader.app.ui.accounts

import com.jocmp.capy.accounts.Source
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val loginModule = module {
    viewModel {
        AddAccountViewModel(
            accountManager = get(),
            appPreferences = get(),
            refreshScheduler = get(),
        )
    }
    viewModel { (source: Source) ->
        LoginViewModel(
            routeSource = source,
            accountManager = get(),
            appPreferences = get(),
            clientCertManager = get(),
            refreshScheduler = get(),
        )
    }
    viewModel {
        UpdateLoginViewModel(
            account = get(),
            clientCertManager = get(),
        )
    }
}
