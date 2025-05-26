package com.capyreader.app.ui

import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.accounts.Source
import kotlinx.serialization.Serializable

sealed class Route {
    @Serializable
    data object AddAccount : Route()

    @Serializable
    data class Login(val source: Source) : Route()

    @Serializable
    data object Settings : Route()

    @Serializable
    data object Articles : Route()

    @Serializable
    data class ArticleListDetail(val filter: ArticleFilter) : Route()

    @Serializable
    data object EmptyArticleListDetail: Route()
}
