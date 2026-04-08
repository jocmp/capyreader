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

    @Serializable
    data object ReadLater : HomePage()

    fun toArticleFilter(readLaterFeedID: String? = null): ArticleFilter {
        return when (this) {
            is Today -> ArticleFilter.Today(todayStatus = ArticleStatus.ALL)
            is Unread -> ArticleFilter.Articles(articleStatus = ArticleStatus.UNREAD)
            is Starred -> ArticleFilter.Starred()
            is ReadLater -> if (readLaterFeedID != null) {
                ArticleFilter.Feeds(
                    feedID = readLaterFeedID,
                    folderTitle = null,
                    feedStatus = ArticleStatus.ALL,
                )
            } else {
                ArticleFilter.Articles(articleStatus = ArticleStatus.UNREAD)
            }
        }
    }

    companion object {
        val default: HomePage = Today
    }
}
