package com.jocmp.capyreader

import com.jocmp.capyreader.refresher.refresherModule
import com.jocmp.capyreader.sync.syncModule
import com.jocmp.capyreader.ui.articles.articlesModule
import com.jocmp.capyreader.ui.accounts.loginModule
import com.jocmp.capyreader.ui.settings.settingsModule
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
