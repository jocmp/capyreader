package com.capyreader.app.ui

import android.net.Uri
import androidx.navigation3.runtime.NavKey
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus

/**
 * Parses a `capy://` deep link into a synthetic Nav3 back stack.
 *
 * The article id is the resource, so it's a path segment (percent-encoded by the caller, since
 * article ids are URLs; [Uri] decodes it back). The feed is optional context as a query param.
 *
 * - `capy://article/<articleID>` (optionally `?feedID=<feedID>`) → list + the article reader.
 *   With `feedID` the list is that feed; without it, the All list.
 * - `capy://articles` → the All list.
 * - `capy://articles/unread` → the Unread list.
 *
 * Returns `null` for anything we don't recognize, so the caller can fall back to its default.
 */
object DeepLink {
    const val SCHEME = "capy"

    /**
     * `capy://article/<articleID>` (optionally `?feedID=`). The article id is a path segment, so
     * [Uri.Builder.appendPath] percent-encodes it (article ids are URLs).
     */
    fun articleUri(articleID: String, feedID: String? = null): Uri =
        Uri.Builder()
            .scheme(SCHEME)
            .authority("article")
            .appendPath(articleID)
            .apply { if (!feedID.isNullOrBlank()) appendQueryParameter("feedID", feedID) }
            .build()

    /** `capy://articles` (All) or `capy://articles/unread` (Unread). */
    fun articlesUri(status: ArticleStatus): Uri =
        Uri.Builder()
            .scheme(SCHEME)
            .authority("articles")
            .apply { if (status == ArticleStatus.UNREAD) appendPath("unread") }
            .build()

    fun parse(uri: Uri?): List<NavKey>? {
        if (uri?.scheme != SCHEME) return null

        return when (uri.host) {
            "article" -> {
                val articleID = uri.pathSegments.firstOrNull() ?: return null
                val feedID = uri.getQueryParameter("feedID")
                val list = if (feedID != null) {
                    Route.ArticleList(
                        ArticleFilter.Feeds(
                            feedID = feedID,
                            folderTitle = null,
                            feedStatus = ArticleStatus.UNREAD,
                        )
                    )
                } else {
                    Route.ArticleList(ArticleFilter.default())
                }
                listOf(list, Route.ArticleDetail(articleID))
            }

            "articles" -> {
                val status = when (uri.pathSegments.firstOrNull()) {
                    "unread" -> ArticleStatus.UNREAD
                    else -> ArticleStatus.ALL
                }
                listOf(Route.ArticleList(ArticleFilter.Articles(status)))
            }

            else -> null
        }
    }
}
