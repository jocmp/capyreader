package com.capyreader.app.ui.addintent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.articles.AddFeedView
import com.capyreader.app.ui.articles.AddFeedViewModel
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.FeedOption
import org.koin.compose.koinInject

@Composable
fun AddLinkScreen(
    addFeedViewModel: AddFeedViewModel = koinInject(),
    savePageViewModel: SavePageViewModel = koinInject(),
    onAddFeedComplete: (feedID: String) -> Unit = {},
    onSavePageComplete: () -> Unit = {},
    onBack: () -> Unit,
    defaultQueryURL: String,
    supportsPages: Boolean,
) {
    AddLinkView(
        defaultQueryURL = defaultQueryURL,
        supportsPages = supportsPages,
        onBack = onBack,
        feedChoices = addFeedViewModel.feedChoices,
        onAddFeed = { url ->
            addFeedViewModel.addFeed(
                url = url,
                onComplete = {
                    addFeedViewModel.selectFeed(it.id)
                    onAddFeedComplete(it.id)
                },
            )
        },
        addFeedLoading = addFeedViewModel.loading,
        addFeedError = addFeedViewModel.error,
        onSavePage = { url ->
            savePageViewModel.savePage(
                url = url,
                onComplete = onSavePageComplete
            )
        },
        savePageLoading = savePageViewModel.loading,
        savePageError = savePageViewModel.error,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddLinkView(
    defaultQueryURL: String,
    supportsPages: Boolean,
    onBack: () -> Unit,
    feedChoices: List<FeedOption> = emptyList(),
    onAddFeed: (url: String) -> Unit = {},
    addFeedLoading: Boolean = false,
    addFeedError: AddFeedResult.Error? = null,
    onSavePage: (url: String) -> Unit = {},
    savePageLoading: Boolean = false,
    savePageError: String? = null,
) {
    val defaultTab = if (supportsPages && defaultQueryURL.isNotBlank()) {
        AddLinkTab.SAVE_PAGE
    } else {
        AddLinkTab.ADD_FEED
    }

    var selectedTab by rememberSaveable { mutableStateOf(defaultTab) }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onBack,
            ),
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.navigationBarsPadding()
            ) {
                if (supportsPages) {
                    val tabs = AddLinkTab.entries

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            ToggleButton(
                                checked = tab == selectedTab,
                                onCheckedChange = { selectedTab = tab },
                                modifier = Modifier.weight(1f),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(tab.icon, contentDescription = null)
                                    Text(stringResource(tab.title))
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        AddLinkTab.ADD_FEED -> {
                            AddFeedView(
                                feedChoices = feedChoices,
                                defaultQueryURL = defaultQueryURL,
                                onAddFeed = onAddFeed,
                                loading = addFeedLoading,
                                error = addFeedError,
                                condensed = false
                            )
                        }

                        AddLinkTab.SAVE_PAGE -> {
                            SavePageView(
                                defaultQueryURL = defaultQueryURL,
                                onSavePage = onSavePage,
                                loading = savePageLoading,
                                error = savePageError,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

private enum class AddLinkTab(val title: Int, val icon: ImageVector) {
    ADD_FEED(title = R.string.tab_add_feed, icon = Icons.Outlined.AddBox),
    SAVE_PAGE(title = R.string.tab_save_page, icon = Icons.Outlined.Bookmarks),
}

@Preview
@Composable
private fun AddLinkViewPreview() {
    AddLinkView(
        defaultQueryURL = "",
        supportsPages = true,
        onBack = {},
    )
}
