package com.capyreader.app.preferences

import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import kotlinx.serialization.Serializable

@Serializable
sealed class HomePage {
    @Serializable
    data object Today : HomePage()

    @Serializable
    data object Unread : HomePage()

    @Serializable
    data object Starred : HomePage()


    fun toArticleFilter(): ArticleFilter {
        return when (this) {
            is Today -> ArticleFilter.Today(todayStatus = ArticleStatus.ALL)
            is Unread -> ArticleFilter.Articles(articleStatus = ArticleStatus.UNREAD)
            is Starred -> ArticleFilter.Starred()
        }
    }

    companion object {
        val default: HomePage = Today
    }
}
