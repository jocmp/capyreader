package com.jocmp.basilreader.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.jocmp.basil.Article
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.components.EmptyView
import com.jocmp.basilreader.ui.components.WebView
import com.jocmp.basilreader.ui.components.WebViewNavigator
import com.jocmp.basilreader.ui.components.WebViewState

private const val TAG = "ArticleView"

@Composable
fun ArticleView(
    article: Article?,
    webViewState: WebViewState,
    webViewNavigator: WebViewNavigator,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit
) {
    if (article != null) {
        ArticleLoadedView(
            article = article,
            onToggleRead = onToggleRead,
            onToggleStar = onToggleStar,
            webViewState = webViewState,
            navigator = webViewNavigator
        )
    } else {
        EmptyView()
    }

    BackHandler(article != null) {
        onBackPressed()
    }
}

@Composable
fun ArticleLoadedView(
    article: Article,
    webViewState: WebViewState,
    navigator: WebViewNavigator,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit
) {
    val readIcon = if (article.read) {
        R.drawable.icon_circle_outline
    } else {
        R.drawable.icon_circle_filled
    }

    val starIcon = if (article.starred) {
        R.drawable.icon_star_filled
    } else {
        R.drawable.icon_star_outline
    }

    Scaffold(
        topBar = {
            Row {
                IconButton(onClick = { onToggleRead() }) {
                    Icon(painterResource(id = readIcon), contentDescription = stringResource(R.string.article_view_mark_as_read))
                }
                IconButton(onClick = { onToggleStar() }) {
                    Icon(painterResource(id = starIcon), contentDescription = stringResource(R.string.article_view_bookmark))
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            WebView(
                state = webViewState,
                navigator = navigator,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
