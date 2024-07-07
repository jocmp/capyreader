package com.capyreader.app.ui.articles

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.capyreader.app.ui.navigationTitle

@Composable
fun FilterAppBarTitle(
    filter: ArticleFilter,
    allFeeds: List<Feed>,
    folders: List<Folder>,
) {
    val text = when (filter) {
        is ArticleFilter.Articles -> stringResource(filter.articleStatus.navigationTitle)
        is ArticleFilter.Feeds -> {
            allFeeds.find { it.id == filter.feedID }?.title
        }
        is ArticleFilter.Folders -> {
            folders.find { it.title == filter.folderTitle }?.title
        }
    }.orEmpty()

    Text(
        text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Preview
@Composable
fun FilterAppBarTitlePreview() {
    MaterialTheme {
        FilterAppBarTitle(
            filter = ArticleFilter.default(),
            allFeeds = emptyList(),
            folders = emptyList(),
        )
    }
}
