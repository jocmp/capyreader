package com.jocmp.basilreader.di

import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.ui.accounts.AccountsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val accountModule = module {
    single { AccountManager(get()) }
    viewModel { AccountsViewModel(get()) }
}
