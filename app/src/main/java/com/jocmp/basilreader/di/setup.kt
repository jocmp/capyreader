package com.jocmp.basilreader.di;

import org.koin.core.KoinApplication

fun KoinApplication.setupModules() {
    modules(accountModule)
}
