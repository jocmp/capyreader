package com.capyreader

import com.capyreader.refresher.refresherModule
import com.capyreader.sync.syncModule
import com.capyreader.ui.articles.articlesModule
import com.capyreader.ui.accounts.loginModule
import com.capyreader.ui.settings.settingsModule
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
