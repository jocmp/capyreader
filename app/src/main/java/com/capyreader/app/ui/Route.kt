package com.capyreader.app.ui

import androidx.navigation3.runtime.NavKey
import com.capyreader.app.common.Media
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.accounts.Source
import kotlinx.serialization.Serializable

sealed class Route : NavKey {
    @Serializable
    data object AddAccount : Route()

    @Serializable
    data class Login(val source: Source) : Route()

    @Serializable
    data object Settings : Route()

    /**
     * The article list. [filter] isn't read by the list entry itself — it's a one-way seed that
     * [com.capyreader.app.MainActivity.applyListFilter] copies into
     * [com.capyreader.app.preferences.AppPreferences.filter] at launch/deep-link time, which is
     * the value the list and reader actually read reactively.
     */
    @Serializable
    data class ArticleList(val filter: ArticleFilter) : Route()

    /** A single article opened in the reader. Resolved from [articleID] by the detail ViewModel. */
    @Serializable
    data class ArticleDetail(val articleID: String) : Route()

    /**
     * Full-screen image viewer, rendered as an overlay above the list/detail panes (see
     * [com.capyreader.app.ui.articles.media.MediaSceneStrategy]). [Media] is already
     * `@Serializable`, so it persists with the back stack across process death.
     */
    @Serializable
    data class MediaViewer(val media: Media) : Route()
}
