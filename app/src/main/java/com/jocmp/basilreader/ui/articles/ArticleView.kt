package com.jocmp.basilreader.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jocmp.basil.Article
import com.jocmp.basilreader.ui.components.EmptyView
import com.jocmp.basilreader.ui.components.WebView
import com.jocmp.basilreader.ui.components.rememberWebViewStateWithHTMLData

@Composable
fun ArticleView(
    article: Article?,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
) {
    if (article != null) {
        ArticleLoadedView(
            article = article,
            onToggleRead = onToggleRead
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
    onToggleRead: () -> Unit,
) {
    val state = rememberWebViewStateWithHTMLData(article.contentHTML)

    val image = if (article.read) {
        Icons.Outlined.CheckCircle
    } else {
        Icons.Filled.CheckCircle
    }

    Scaffold(
        topBar = {
            Button(onClick = { onToggleRead() }) {
                Icon(imageVector = image, contentDescription = null)
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            WebView(state)
        }
    }
}
