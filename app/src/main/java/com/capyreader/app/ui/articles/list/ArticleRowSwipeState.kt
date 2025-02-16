package com.capyreader.app.ui.articles.list

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.common.asState
import com.capyreader.app.common.openLinkExternally
import com.capyreader.app.ui.articles.LocalArticleActions
import com.capyreader.app.ui.components.ArticleAction
import com.capyreader.app.ui.components.readAction
import com.capyreader.app.ui.components.starAction
import com.capyreader.app.preferences.RowSwipeOption
import com.jocmp.capy.Article
import me.saket.swipe.SwipeAction
import org.koin.compose.koinInject

internal data class ArticleRowSwipeState(
    val start: List<SwipeAction>,
    val end: List<SwipeAction>,
) {
    val disabled = start.isEmpty() && end.isEmpty()
}

@Composable
internal fun rememberArticleRowSwipeState(
    article: Article,
    appPreferences: AppPreferences = koinInject(),
): ArticleRowSwipeState {
    val swipeStart by appPreferences.articleListOptions.swipeStart.asState()
    val swipeEnd by appPreferences.articleListOptions.swipeEnd.asState()

    val start = swipeActions(article, swipeStart)
    val end = swipeActions(article, swipeEnd)

    return ArticleRowSwipeState(
        start = start,
        end = end,
    )
}

@Composable
private fun swipeActions(article: Article, option: RowSwipeOption): List<SwipeAction> {
    if (option == RowSwipeOption.DISABLED) {
        return emptyList()
    }

    val actions = LocalArticleActions.current
    val context = LocalContext.current

    val action = when (option) {
        RowSwipeOption.TOGGLE_STARRED -> starAction(article, actions)
        RowSwipeOption.OPEN_EXTERNALLY -> openExternally(context, article)
        else -> readAction(article, actions)
    }

    return listOf(
        SwipeAction(
            onSwipe = action.commit,
            background = MaterialTheme.colorScheme.surfaceContainerHighest,
            icon = {
                Box(Modifier.padding(16.dp)) {
                    Icon(
                        painterResource(action.icon),
                        contentDescription = stringResource(action.translationKey)
                    )
                }
            },
        )
    )
}


private fun openExternally(context: Context, article: Article) =
    ArticleAction(
        R.drawable.icon_open_in_new,
        R.string.article_view_open_externally,
    ) {
        val url = article.url ?: return@ArticleAction

        context.openLinkExternally(url)
    }
