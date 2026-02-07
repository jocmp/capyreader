package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.articles.feeds.DrawerItem
import com.capyreader.app.ui.articles.list.SavedSearchActionMenu
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.SavedSearch

@Composable
fun SavedSearchRow(
    onSelect: (savedSearch: SavedSearch) -> Unit,
    selected: Boolean,
    savedSearch: SavedSearch,
    status: ArticleStatus = ArticleStatus.ALL,
) {
    val (showMenu, setShowMenu) = remember { mutableStateOf(false) }

    Box {
        DrawerItem(
            label = { ListTitle(savedSearch.name) },
            badge = {
                CountBadge(count = savedSearch.count, showBadge = savedSearch.showUnreadBadge, status = status)
            },
            selected = selected,
            onClick = {
                onSelect(savedSearch)
            },
            onLongClick = {
                setShowMenu(true)
            }
        )

        SavedSearchActionMenu(
            expanded = showMenu,
            savedSearch = savedSearch,
            onDismissMenuRequest = { setShowMenu(false) },
        )
    }
}

@Preview
@Composable
fun SavedSearchRowPreview() {
    val savedSearch = SavedSearch(
        id = "123",
        name = "Galaxy S25",
        query = null,
    )

    MaterialTheme {
        SavedSearchRow (
            savedSearch = savedSearch,
            onSelect = {},
            selected = false
        )
    }
}
