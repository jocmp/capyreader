package com.jocmp.basilreader.ui.articles

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.jocmp.basil.ArticleFilter
import com.jocmp.basilreader.ui.navigationTitle

@Composable
fun FilterAppBarTitle(filter: ArticleFilter) {
    val text = when (filter) {
        is ArticleFilter.Articles -> stringResource(filter.articleStatus.navigationTitle)
        is ArticleFilter.Feeds -> filter.feed.name
        is ArticleFilter.Folders -> filter.folder.title
    }

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
        FilterAppBarTitle(ArticleFilter.default())
    }
}
