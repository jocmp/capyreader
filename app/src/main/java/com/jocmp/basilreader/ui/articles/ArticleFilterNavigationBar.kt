package com.jocmp.basilreader.ui.articles

import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basil.ArticleStatus
import com.jocmp.basilreader.R

@Composable
fun ArticleFilterNavigationBar(
    selected: ArticleStatus,
    onSelect: (status: ArticleStatus) -> Unit
) {
    val checkedSelect = { status: ArticleStatus ->
        if (selected != status) {
            onSelect(status)
        }
    }

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
            label = { Text(stringResource(id = R.string.article_filters_starred)) },
            selected = selected === ArticleStatus.STARRED,
            onClick = { checkedSelect(ArticleStatus.STARRED) },
            alwaysShowLabel = false
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.unread),
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.article_filters_unread)) },
            selected = selected === ArticleStatus.UNREAD,
            onClick = { checkedSelect(ArticleStatus.UNREAD) },
            alwaysShowLabel = false
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.notes),
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.article_filters_all)) },
            selected = selected === ArticleStatus.ALL,
            onClick = { checkedSelect(ArticleStatus.ALL) },
            alwaysShowLabel = false
        )
    }
}

@Preview
@Composable
fun ArticleFilterNavigationBarPreview() {
    ArticleFilterNavigationBar(
        selected = ArticleStatus.ALL,
        onSelect = {}
    )
}
