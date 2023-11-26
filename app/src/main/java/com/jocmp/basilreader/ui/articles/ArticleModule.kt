package com.jocmp.basilreader.ui.articles

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val articleModule = module {
    viewModel { AccountViewModel(get(), get()) }
}
