package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jocmp.basilreader.ui.components.EmptyView
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditFeedScreen(
    viewModel: EditFeedViewModel = koinViewModel(),
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    val feed = viewModel.feed

    Card(
        shape = RoundedCornerShape(16.dp)
    ) {
        feed?.let {
            EditFeedView(
                feed = feed,
                folders = viewModel.folders,
                feedFoldersTitles = viewModel.feedFolderTitles,
                onSubmit = { entry ->
                    viewModel.submit(entry, onSubmit)
                },
                onCancel = onCancel
            )
        } ?: EmptyView(showLoading = true)
    }
}
