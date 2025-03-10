package com.capyreader.app.ui.addintent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.ui.articles.AddFeedView
import com.capyreader.app.ui.articles.AddFeedViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedScreen(
    viewModel: AddFeedViewModel = koinInject(),
    defaultQueryURL: String,
    onComplete: (feedID: String) -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_feed_screen_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
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
