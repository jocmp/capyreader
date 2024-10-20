package com.capyreader.app.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.capyreader.app.R
import com.capyreader.app.ui.articles.ArticleActions
import com.jocmp.capy.Article

data class ArticleAction(
    @DrawableRes val icon: Int,
    @StringRes val translationKey: Int,
    val commit: () -> Unit,
)

fun readAction(article: Article, actions: ArticleActions) =
    if (article.read) {
        ArticleAction(
            R.drawable.icon_circle_filled,
            R.string.article_view_mark_as_unread,
        ) {
            actions.markUnread(article.id)
        }
    } else {
        ArticleAction(
            R.drawable.icon_circle_outline,
            R.string.article_view_mark_as_read,
        ) {
            actions.markRead(article.id)
        }
    }

fun starAction(article: Article, actions: ArticleActions) =
    if (article.starred) {
        ArticleAction(
            R.drawable.icon_star_outline,
            R.string.article_view_unstar,
        ) {
            actions.unstar(article.id)
        }
    } else {
        ArticleAction(
            R.drawable.icon_star_filled,
            R.string.article_view_star,
        ) {
            actions.star(article.id)
        }
    }
