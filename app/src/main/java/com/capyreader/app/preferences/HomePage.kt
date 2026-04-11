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
    data class ReadLater(val feedID: String) : HomePage()

    fun toArticleFilter(): ArticleFilter {
        return when (this) {
            is Today -> ArticleFilter.Today(todayStatus = ArticleStatus.ALL)
            is Unread -> ArticleFilter.Unread
            is Starred -> ArticleFilter.Starred
            is ReadLater -> ArticleFilter.Feeds(
                feedID = feedID,
                folderTitle = null,
                feedStatus = ArticleStatus.ALL,
            )
        }
    }

    companion object {
        val default: HomePage = Today
    }
}
