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

    @Serializable
    data object Articles : Route()

    /**
     * The article list, parameterized by its [filter]. Filter is navigation state;
     * [com.capyreader.app.preferences.AppPreferences.filter] is demoted to a cold-boot seed.
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
