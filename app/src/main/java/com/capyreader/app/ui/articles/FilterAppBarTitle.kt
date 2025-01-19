package com.capyreader.app.ui.articles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.navigationTitle
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.SavedSearch

@Composable
fun FilterAppBarTitle(
    filter: ArticleFilter,
    allFeeds: List<Feed>,
    allFolders: List<Folder>,
    allSavedSearches: List<SavedSearch>,
    onRequestJumpToTop: () -> Unit
) {
    val text = when (filter) {
        is ArticleFilter.Articles -> stringResource(filter.articleStatus.navigationTitle)
        is ArticleFilter.Feeds -> {
            allFeeds.find { it.id == filter.feedID }?.title
        }

        is ArticleFilter.Folders -> {
            allFolders.find { it.title == filter.folderTitle }?.title
        }

        is ArticleFilter.SavedSearches ->
            allSavedSearches.find { it.id == filter.savedSearchID }?.name
    }.orEmpty()

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onRequestJumpToTop()
            }
    ) {
        Text(
            text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun FilterAppBarTitlePreview() {
    MaterialTheme {
        TopAppBar(title = {
            FilterAppBarTitle(
                filter = ArticleFilter.default(),
                allFeeds = emptyList(),
                allFolders = emptyList(),
                allSavedSearches = emptyList(),
                onRequestJumpToTop = {}
            )
        })
    }
}
