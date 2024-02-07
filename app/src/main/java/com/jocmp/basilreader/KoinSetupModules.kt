package com.jocmp.basilreader

import com.jocmp.basilreader.refresher.refresherModule
import com.jocmp.basilreader.ui.accounts.accountModule
import com.jocmp.basilreader.ui.articles.articlesModule
import org.koin.core.KoinApplication

fun KoinApplication.setupModules() {
    modules(common)
    modules(accountModule)
    modules(articlesModule)
    modules(refresherModule)
}
