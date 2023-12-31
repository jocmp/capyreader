package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.basil.ArticleFilter
import com.jocmp.basilreader.R

@Composable
fun ArticleFilterNavigationBar(
    selected: ArticleFilter.Status,
    onSelect: (status: ArticleFilter.Status) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
            label = { Text(stringResource(id = R.string.article_filters_starred)) },
            selected = selected === ArticleFilter.Status.STARRED,
            onClick = { onSelect(ArticleFilter.Status.STARRED) },
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
            selected = selected === ArticleFilter.Status.UNREAD,
            onClick = { onSelect(ArticleFilter.Status.UNREAD) },
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
            selected = selected === ArticleFilter.Status.ALL,
            onClick = { onSelect(ArticleFilter.Status.ALL) },
            alwaysShowLabel = false
        )
    }
}

@Preview
@Composable
fun ArticleFilterNavigationBarPreview() {
    ArticleFilterNavigationBar(
        selected = ArticleFilter.Status.ALL,
        onSelect = {}
    )
}
