package com.jocmp.basilreader

import com.jocmp.basilreader.refresher.refresherModule
import com.jocmp.basilreader.sync.syncModule
import com.jocmp.basilreader.ui.articles.articlesModule
import com.jocmp.basilreader.ui.login.loginModule
import com.jocmp.basilreader.ui.settings.settingsModule
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
