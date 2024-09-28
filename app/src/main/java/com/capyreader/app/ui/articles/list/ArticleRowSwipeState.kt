package com.capyreader.app.ui.articles.list

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.capyreader.app.R
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.RowSwipeOption
import com.capyreader.app.common.asState
import com.capyreader.app.ui.articles.ArticleActions
import com.capyreader.app.ui.articles.LocalArticleActions
import com.capyreader.app.ui.articles.list.ArticleRowSwipeState.SwipeAction
import com.jocmp.capy.Article
import org.koin.compose.koinInject

internal data class ArticleRowSwipeState(
    val state: SwipeToDismissBoxState,
    val action: SwipeAction,
    val enableStart: Boolean,
    val enableEnd: Boolean,
) {
    data class SwipeAction(
        val icon: ImageVector,
        @StringRes val translationKey: Int,
        val commit: () -> Unit,
    )
}

@Composable
internal fun rememberArticleRowSwipeState(
    article: Article,
    appPreferences: AppPreferences = koinInject(),
): ArticleRowSwipeState {
    val actions = LocalArticleActions.current
    val state = rememberSwipeToDismissBoxState()
    val swipeStart by appPreferences.articleListOptions.swipeStart.asState()
    val swipeEnd by appPreferences.articleListOptions.swipeEnd.asState()

    return remember(state.currentValue, state.dismissDirection, swipeStart, swipeEnd) {
        val preference = swipePreference(state, swipeStart, swipeEnd)

        val swipeAction = when (preference) {
            RowSwipeOption.TOGGLE_STARRED -> starAction(article, actions)
            else -> readAction(article, actions)
        }

        ArticleRowSwipeState(
            state,
            swipeAction,
            enableStart = swipeStart != RowSwipeOption.DISABLED,
            enableEnd = swipeEnd != RowSwipeOption.DISABLED,
        )
    }
}

fun swipePreference(
    state: SwipeToDismissBoxState,
    swipeStart: RowSwipeOption,
    swipeEnd: RowSwipeOption,
): RowSwipeOption {
    return when (state.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> swipeStart
        else -> swipeEnd
    }
}

private fun starAction(article: Article, actions: ArticleActions): SwipeAction {
    return when {
        article.starred -> SwipeAction(
            Icons.Rounded.StarOutline,
            R.string.article_view_unstar,
        ) {
            actions.unstar(article.id)
        }

        else -> SwipeAction(
            Icons.Rounded.Star,
            R.string.article_view_star,
        ) {
            actions.star(article.id)
        }
    }
}

private fun readAction(article: Article, actions: ArticleActions): SwipeAction {
    return when {
        article.read -> SwipeAction(
            Icons.Rounded.Circle,
            R.string.article_view_mark_as_unread,
        ) {
            actions.markUnread(article.id)
        }

        else -> SwipeAction(
            Icons.Outlined.Circle,
            R.string.article_view_mark_as_read,
        ) {
            actions.markRead(article.id)
        }
    }
}
