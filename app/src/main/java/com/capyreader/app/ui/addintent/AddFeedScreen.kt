package com.capyreader.app.ui.addintent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.ui.articles.AddFeedView
import com.capyreader.app.ui.articles.AddFeedViewModel
import com.capyreader.app.ui.components.Spacing
import com.capyreader.app.ui.components.safeEdgePadding
import com.capyreader.app.widthMaxSingleColumn
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedScreen(
    viewModel: AddFeedViewModel = koinInject(),
    onComplete: (feedID: String) -> Unit = {},
    onBack: () -> Unit,
    defaultQueryURL: String,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_add_feed)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .safeEdgePadding(),
    ) { padding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .widthMaxSingleColumn()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = Spacing.topBarHeight)
                    .imePadding()
            ) {
                AddFeedView(
                    feedChoices = viewModel.feedChoices,
                    defaultQueryURL = defaultQueryURL,
                    onAddFeed = { url ->
                        viewModel.addFeed(
                            url = url,
                            onComplete = {
                                viewModel.selectFeed(it.id)
                                onComplete(it.id)
                            },
                        )
                    },
                    loading = viewModel.loading,
                    error = viewModel.error,
                    condensed = false
                )
            }
        }
    }
}
