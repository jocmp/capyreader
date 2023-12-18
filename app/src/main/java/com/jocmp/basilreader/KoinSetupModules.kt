package com.jocmp.basilreader;

import com.jocmp.basilreader.ui.accounts.accountModule

import org.koin.core.KoinApplication


fun KoinApplication.setupModules() {
    modules(accountModule)
}
