package com.capyreader.app.ui.articles.list

import android.content.Context
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.capyreader.app.R
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.asState
import com.capyreader.app.common.openLinkExternally
import com.capyreader.app.ui.articles.LocalArticleActions
import com.capyreader.app.ui.components.ArticleAction
import com.capyreader.app.ui.components.readAction
import com.capyreader.app.ui.components.rememberNoFlingSwipeToDismissBoxState
import com.capyreader.app.ui.components.starAction
import com.capyreader.app.ui.settings.panels.RowSwipeOption
import com.jocmp.capy.Article
import org.koin.compose.koinInject

internal data class ArticleRowSwipeState(
    val state: SwipeToDismissBoxState,
    val action: ArticleAction,
    val enableStart: Boolean,
    val enableEnd: Boolean,
) {
    val enabled = enableStart || enableEnd
}

@Composable
internal fun rememberArticleRowSwipeState(
    article: Article,
    appPreferences: AppPreferences = koinInject(),
): ArticleRowSwipeState {
    val actions = LocalArticleActions.current
    val state = rememberNoFlingSwipeToDismissBoxState()
    val context = LocalContext.current
    val swipeStart by appPreferences.articleListOptions.swipeStart.asState()
    val swipeEnd by appPreferences.articleListOptions.swipeEnd.asState()

    return remember(state.currentValue, state.dismissDirection, swipeStart, swipeEnd) {
        val preference = swipePreference(state, swipeStart, swipeEnd)

        val swipeAction = when (preference) {
            RowSwipeOption.TOGGLE_STARRED -> starAction(article, actions)
            RowSwipeOption.OPEN_EXTERNALLY -> openExternally(context, article)
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

private fun openExternally(context: Context, article: Article) =
    ArticleAction(
        R.drawable.icon_open_in_new,
        R.string.article_view_open_externally,
    ) {
        context.openLinkExternally(article.url)
    }
