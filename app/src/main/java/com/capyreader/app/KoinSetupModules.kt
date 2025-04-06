package com.capyreader.app

import com.capyreader.app.refresher.refresherModule
import com.capyreader.app.sync.syncModule
import com.capyreader.app.ui.articles.articlesModule
import com.capyreader.app.ui.accounts.loginModule
import com.capyreader.app.ui.settings.settingsModule
import org.koin.core.KoinApplication
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

fun KoinApplication.setupCommonModules() {
    modules(common, loginModule)
}

fun loadAccountModules() {
    loadKoinModules(accountModules)
}

fun unloadAccountModules() {
    unloadKoinModules(accountModules)
}

private val accountModules = listOf(
    accountModule,
    settingsModule,
    articlesModule,
    refresherModule,
    syncModule
)
