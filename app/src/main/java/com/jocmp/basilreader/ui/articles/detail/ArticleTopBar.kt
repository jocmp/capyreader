package com.jocmp.basilreader.ui.articles.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.jocmp.basil.Article
import com.jocmp.basilreader.R
import com.jocmp.basilreader.ui.LocalWindowWidth
import com.jocmp.basilreader.ui.fixtures.ArticleSample

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleTopBar(
    article: Article,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    onClose: () -> Unit
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

    TopAppBar(
        navigationIcon = {
            ArticleNavigationIcon(
                onClick = onClose
            )
        },
        title = {},
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onToggleRead() }) {
                    Icon(
                        painterResource(id = readIcon),
                        contentDescription = stringResource(R.string.article_view_mark_as_read)
                    )
                }
                IconButton(onClick = { onToggleStar() }) {
                    Icon(
                        painterResource(id = starIcon),
                        contentDescription = stringResource(R.string.article_view_star)
                    )
                }
            }
        }
    )

}

@Composable
fun ArticleNavigationIcon(onClick: () -> Unit) {
    val showIcon = LocalWindowWidth.current == WindowWidthSizeClass.Compact

    if (!showIcon) {
        return
    }

    IconButton(
        onClick = {
            onClick()
        }
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun ArticleTopBarPreview(@PreviewParameter(ArticleSample::class) article: Article) {
    ArticleTopBar(
        article = article,
        onToggleRead = {},
        onToggleStar = {},
        onClose = {},
    )
}

@Preview
@Composable
private fun ArticleTopBarPreview_Tablet(@PreviewParameter(ArticleSample::class) article: Article) {
    val width = WindowWidthSizeClass.Medium

    CompositionLocalProvider(LocalWindowWidth provides width) {
        ArticleTopBar(
            article = article,
            onToggleRead = {},
            onToggleStar = {},
            onClose = {}
        )
    }
}
