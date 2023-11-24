package com.jocmp.basilreader.ui.accounts

import com.jocmp.basil.AccountManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val accountModule = module {
    single { AccountManager(androidContext().filesDir.toURI()) }
    viewModel { AccountIndexViewModel(get()) }
}
