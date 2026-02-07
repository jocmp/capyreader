package com.capyreader.app.ui.addintent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.articles.AddFeedView
import com.capyreader.app.ui.articles.AddFeedViewModel
import com.capyreader.app.ui.components.safeEdgePadding
import com.capyreader.app.widthMaxSingleColumn
import org.koin.compose.koinInject

private const val TAB_ADD_FEED = 0
private const val TAB_SAVE_PAGE = 1

@OptIn(ExperimentalMaterial3Api::class)
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
    val defaultTab = if (supportsPages && defaultQueryURL.isNotBlank()) {
        TAB_SAVE_PAGE
    } else {
        TAB_ADD_FEED
    }

    val (selectedTab, setSelectedTab) = rememberSaveable { mutableIntStateOf(defaultTab) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .safeEdgePadding(),
    ) {
        Card(
            modifier = Modifier
                .widthMaxSingleColumn()
                .padding(16.dp),
        ) {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.add_link_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                )

                if (supportsPages) {
                    PrimaryTabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == TAB_ADD_FEED,
                            onClick = { setSelectedTab(TAB_ADD_FEED) },
                            text = { Text(stringResource(R.string.tab_add_feed)) }
                        )
                        Tab(
                            selected = selectedTab == TAB_SAVE_PAGE,
                            onClick = { setSelectedTab(TAB_SAVE_PAGE) },
                            text = { Text(stringResource(R.string.tab_save_page)) }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        TAB_ADD_FEED -> {
                            AddFeedView(
                                feedChoices = addFeedViewModel.feedChoices,
                                defaultQueryURL = defaultQueryURL,
                                onAddFeed = { url ->
                                    addFeedViewModel.addFeed(
                                        url = url,
                                        onComplete = {
                                            addFeedViewModel.selectFeed(it.id)
                                            onAddFeedComplete(it.id)
                                        },
                                    )
                                },
                                loading = addFeedViewModel.loading,
                                error = addFeedViewModel.error,
                                condensed = false
                            )
                        }

                        TAB_SAVE_PAGE -> {
                            SavePageView(
                                defaultQueryURL = defaultQueryURL,
                                onSavePage = { url ->
                                    savePageViewModel.savePage(
                                        url = url,
                                        onComplete = onSavePageComplete
                                    )
                                },
                                loading = savePageViewModel.loading,
                                error = savePageViewModel.error,
                            )
                        }
                    }
                }
            }
        }
    }
}
