package com.jocmp.basilreader;

import com.jocmp.basilreader.ui.accounts.accountModule
import com.jocmp.basilreader.ui.articles.articleModule
import org.koin.core.KoinApplication

fun KoinApplication.setupModules() {
    modules(accountModule)
    modules(articleModule)
}
