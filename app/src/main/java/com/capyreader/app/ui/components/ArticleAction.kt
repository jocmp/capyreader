package com.capyreader.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.ui.graphics.vector.ImageVector
import com.capyreader.app.R
import com.capyreader.app.ui.articles.ArticleActions
import com.jocmp.capy.Article

data class ArticleAction(
    val icon: ImageVector,
    @StringRes val translationKey: Int,
    val commit: () -> Unit,
)

fun readAction(article: Article, actions: ArticleActions) =
    if (article.read) {
        ArticleAction(
            Icons.Outlined.FiberManualRecord,
            R.string.article_view_mark_as_unread,
        ) {
            actions.markUnread(article.id)
        }
    } else {
        ArticleAction(
            Icons.Rounded.FiberManualRecord,
            R.string.article_view_mark_as_read,
        ) {
            actions.markRead(article.id)
        }
    }

fun starAction(article: Article, actions: ArticleActions) =
    if (article.starred) {
        ArticleAction(
            Icons.Rounded.StarOutline,
            R.string.article_view_unstar,
        ) {
            actions.unstar(article.id)
        }
    } else {
        ArticleAction(
            Icons.Rounded.Star,
            R.string.article_view_star,
        ) {
            actions.star(article.id)
        }
    }
