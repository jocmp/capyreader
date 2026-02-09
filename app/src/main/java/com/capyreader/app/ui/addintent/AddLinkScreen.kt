package com.capyreader.app.ui.addintent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.FeedOption
import org.koin.compose.koinInject

@Composable
fun AddLinkScreen(
    viewModel: AddLinkViewModel = koinInject(),
    onBack: () -> Unit,
    defaultQueryURL: String,
    pageTitle: String,
    supportsPages: Boolean,
) {
    val (successMessage, setSuccessMessage) = remember { mutableStateOf<String?>(null) }

    AddLinkView(
        defaultQueryURL = defaultQueryURL,
        pageTitle = pageTitle,
        supportsPages = supportsPages,
        onBack = onBack,
        feedChoices = viewModel.feedChoices,
        onSearchFeed = { url ->
            viewModel.searchFeed(url = url)
        },
        onAddFeed = { url ->
            viewModel.addFeed(
                url = url,
                onComplete = {
                    viewModel.selectFeed(it.id)
                    setSuccessMessage("feed")
                },
            )
        },
        addFeedLoading = viewModel.feedLoading,
        addFeedError = viewModel.feedError,
        onSavePage = {
            viewModel.savePage(
                url = defaultQueryURL,
                onComplete = {
                    setSuccessMessage("page")
                }
            )
        },
        savePageLoading = viewModel.pageLoading,
        savePageError = viewModel.pageError,
        successMessage = successMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddLinkView(
    defaultQueryURL: String,
    pageTitle: String = "",
    supportsPages: Boolean,
    onBack: () -> Unit,
    feedChoices: List<FeedOption> = emptyList(),
    onSearchFeed: (url: String) -> Unit = {},
    onAddFeed: (url: String) -> Unit = {},
    addFeedLoading: Boolean = false,
    addFeedError: AddFeedResult.Error? = null,
    onSavePage: () -> Unit = {},
    savePageLoading: Boolean = false,
    savePageError: String? = null,
    successMessage: String? = null,
) {
    val defaultTab = if (supportsPages && defaultQueryURL.isNotBlank()) {
        AddLinkTab.SAVE_PAGE
    } else {
        AddLinkTab.ADD_FEED
    }

    val (selectedTab, setSelectedTab) = rememberSaveable { mutableStateOf(defaultTab) }
    val (feedSearchTriggered, setFeedSearchTriggered) = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (defaultQueryURL.isNotBlank() && !feedSearchTriggered) {
            setFeedSearchTriggered(true)
            onSearchFeed(defaultQueryURL)
        }
    }

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
                if (supportsPages && successMessage == null) {
                    val tabs = AddLinkTab.entries

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        tabs.forEach { tab ->
                            ToggleButton(
                                checked = tab == selectedTab,
                                onCheckedChange = { setSelectedTab(tab) },
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
                        .heightIn(min = 120.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (successMessage != null) {
                        SuccessView(message = successMessage)
                    } else {
                        when (selectedTab) {
                            AddLinkTab.ADD_FEED -> {
                                if (addFeedLoading && feedChoices.isEmpty() && addFeedError == null) {
                                    SearchingView()
                                } else {
                                    SubscribeView(
                                        feedChoices = feedChoices,
                                        onSubscribe = onAddFeed,
                                        loading = addFeedLoading,
                                        error = addFeedError,
                                    )
                                }
                            }

                            AddLinkTab.SAVE_PAGE -> {
                                SavePageView(
                                    pageTitle = pageTitle,
                                    url = defaultQueryURL,
                                    onSavePage = onSavePage,
                                    loading = savePageLoading,
                                    error = savePageError,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchingView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
        Text(
            text = stringResource(R.string.add_link_searching),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SuccessView(message: String) {
    val text = when (message) {
        "feed" -> stringResource(R.string.add_link_feed_added)
        else -> stringResource(R.string.add_link_page_saved)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
        )
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
        defaultQueryURL = "https://example.com",
        supportsPages = true,
        onBack = {},
    )
}
