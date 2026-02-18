package com.capyreader.app

import com.capyreader.app.refresher.refresherModule
import com.capyreader.app.sync.syncModule
import com.capyreader.app.ui.accounts.loginModule
import com.capyreader.app.ui.articles.articlesModule
import com.capyreader.app.ui.settings.settingsModule
import org.koin.core.KoinApplication
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

fun KoinApplication.setupCommonModules() {
    modules(common, loginModule)
}

private var currentAccountModules: List<org.koin.core.module.Module>? = null

fun loadAccountModules(accountID: String) {
    val modules = accountModules(accountID)
    currentAccountModules = modules
    loadKoinModules(modules)
}

fun unloadAccountModules() {
    currentAccountModules?.let { unloadKoinModules(it) }
    currentAccountModules = null
}

private fun accountModules(accountID: String) = listOf(
    accountModule(accountID),
    settingsModule,
    articlesModule,
    refresherModule,
    syncModule,
)
