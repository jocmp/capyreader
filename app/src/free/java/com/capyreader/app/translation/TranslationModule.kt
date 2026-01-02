package com.capyreader.app.translation

import org.koin.dsl.module

val translationModule = module {
    single<ArticleTranslator> { NoOpArticleTranslator() }
}
