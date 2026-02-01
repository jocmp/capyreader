package com.capyreader.app.ui.articles

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.articles.feeds.DrawerItem
import com.jocmp.capy.SavedSearch

@Composable
fun SavedSearchRow(
    onSelect: (savedSearch: SavedSearch) -> Unit,
    selected: Boolean,
    savedSearch: SavedSearch,
) {
    DrawerItem(
        label = { ListTitle(savedSearch.name) },
        badge = { CountBadge(count = savedSearch.count) },
        selected = selected,
        onClick = {
            onSelect(savedSearch)
        }
    )
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
