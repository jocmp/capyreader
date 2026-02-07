package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.preferences.CountStyle
import com.capyreader.app.ui.LocalCountStyle
import com.capyreader.app.ui.articles.CountBadge
import com.capyreader.app.ui.articles.FaviconBadge
import com.capyreader.app.ui.savedSearchNavTitle
import com.capyreader.app.ui.settings.PreferenceSelect
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.Feed
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.accounts.Source

@Composable
fun UnreadBadgesSettingsPanel(
    countStyle: CountStyle,
    updateCountStyle: (CountStyle) -> Unit,
    source: Source,
    feeds: List<Feed>,
    savedSearches: List<SavedSearch>,
    onSelectAll: () -> Unit,
    onSelectNone: () -> Unit,
    onToggleFeed: (feedID: String, enabled: Boolean) -> Unit,
    onToggleSavedSearch: (id: String, enabled: Boolean) -> Unit,
) {
    val groupSelection = remember(feeds, savedSearches) {
        val enabled = feeds.count { it.showUnreadCounts } + savedSearches.count { it.showUnreadCounts }
        val total = feeds.size + savedSearches.size

        when (enabled) {
            0 -> ToggleableState.Off
            total -> ToggleableState.On
            else -> ToggleableState.Indeterminate
        }
    }

    LazyColumn {
        item {
            PreferenceSelect(
                selected = countStyle,
                update = updateCountStyle,
                options = CountStyle.entries,
                label = R.string.settings_option_count_style,
                optionText = {
                    stringResource(it.translationKey)
                },
                trailingContent = {
                    Box(
                        Modifier.padding(end = 20.dp)
                    ) {
                        CompositionLocalProvider(LocalCountStyle provides countStyle) {
                            CountBadge(64)
                        }
                    }
                }
            )
        }

        if (countStyle == CountStyle.SIMPLE) {
            item {
                UnreadBadgesGroupCheckbox(
                    onSelectAll = onSelectAll,
                    onSelectNone = onSelectNone,
                    groupSelection = groupSelection,
                )
            }

            if (savedSearches.isNotEmpty()) {
                stickyHeader {
                    SectionHeader(stringResource(source.savedSearchNavTitle))
                }

                items(savedSearches, key = { "savedSearch:${it.id}" }) { savedSearch ->
                    FeedSettingsCheckbox(
                        title = savedSearch.name,
                        checked = savedSearch.showUnreadCounts,
                        onToggle = { onToggleSavedSearch(savedSearch.id, it) },
                    )
                }
            }

            if (feeds.isNotEmpty()) {
                stickyHeader {
                    SectionHeader(stringResource(R.string.nav_headline_feeds))
                }

                items(feeds, key = { "feed:${it.id}" }) { feed ->
                    FeedSettingsCheckbox(
                        title = feed.title,
                        checked = feed.showUnreadCounts,
                        onToggle = { onToggleFeed(feed.id, it) },
                        leadingContent = { FaviconBadge(feed.faviconURL) },
                    )
                }
            }

            item {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colorScheme.surface,
    ) {
        Text(
            text = title,
            style = typography.labelLarge,
            color = colorScheme.surfaceTint,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun UnreadBadgesGroupCheckbox(
    onSelectAll: () -> Unit,
    onSelectNone: () -> Unit,
    groupSelection: ToggleableState,
) {
    val onClick = {
        when (groupSelection) {
            ToggleableState.On -> onSelectNone()
            else -> onSelectAll()
        }
    }

    Box(
        Modifier.clickable {
            onClick()
        }
    ) {
        ListItem(
            headlineContent = {
                val text = when (groupSelection) {
                    ToggleableState.On -> stringResource(R.string.settings_select_none)
                    else -> stringResource(R.string.settings_select_all)
                }

                Text(text, fontWeight = FontWeight.Medium)
            },
            trailingContent = {
                TriStateCheckbox(
                    state = groupSelection,
                    onClick = onClick,
                )
            }
        )
    }
}


@Preview
@Composable
private fun UnreadBadgesSettingsPanelViewPreview() {
    CapyTheme {
        Surface {
            UnreadBadgesSettingsPanel(
                countStyle = CountStyle.SIMPLE,
                updateCountStyle = {},
                feeds = listOf(
                    Feed(
                        id = "1",
                        subscriptionID = "1",
                        title = "Ars Technica",
                        feedURL = "https://example.com/feed",
                        showUnreadCounts = true,
                    ),
                    Feed(
                        id = "2",
                        subscriptionID = "2",
                        title = "The Verge",
                        feedURL = "https://example.com/feed2",
                        showUnreadCounts = false,
                    ),
                ),
                savedSearches = listOf(
                    SavedSearch(id = "1", name = "Galaxy S25", query = null),
                ),
                onSelectAll = {},
                onSelectNone = {},
                onToggleFeed = { _, _ -> },
                onToggleSavedSearch = { _, _ -> },
                source = Source.FRESHRSS
            )
        }
    }
}
