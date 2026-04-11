package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.capyreader.app.R
import com.capyreader.app.ui.LocalMarkAllReadButtonPosition
import com.capyreader.app.ui.articles.list.MarkAllReadButton
import com.capyreader.app.ui.fixtures.FeedSample
import com.capyreader.app.ui.fixtures.PreviewKoinApplication
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed

@Composable
fun FilterActionMenu(
    filter: ArticleFilter,
    onRequestSearch: () -> Unit,
    hideSearchIcon: Boolean,
) {
    val markReadPosition = LocalMarkAllReadButtonPosition.current

    Row {
        if (!hideSearchIcon) {
            IconButton(onClick = onRequestSearch) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(R.string.filter_action_menu_search_articles)
                )
            }
        }

        if (markReadPosition == MarkReadPosition.TOOLBAR) {
            MarkAllReadButton()
        }
    }
}

@Preview
@Composable
fun FeedActionsPreview(@PreviewParameter(FeedSample::class) feed: Feed) {
    PreviewKoinApplication {
        FilterActionMenu(
            onRequestSearch = {},
            filter = ArticleFilter.Feeds(
                feedID = feed.id,
                folderTitle = null,
                feedStatus = ArticleStatus.ALL
            ),
            hideSearchIcon = true,
        )
    }
}
