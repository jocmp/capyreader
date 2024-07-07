package com.capyreader.app.ui.articles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jocmp.capy.EditFeedFormEntry
import com.jocmp.capy.Feed
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditFeedDialog(
    feed: Feed,
    form: EditFeedViewModel = koinViewModel(),
    onSubmit: () -> Unit,
    onFailure: () -> Unit,
    onCancel: () -> Unit
) {
    val folders by form.folders.collectAsState(initial = listOf())
    val submit = { entry: EditFeedFormEntry ->
        form.submit(entry, onSubmit, onFailure)
    }

    Dialog(onDismissRequest = onCancel) {
        Card {
            EditFeedView(
                feed = feed,
                folders = folders,
                onSubmit = submit,
                onCancel = onCancel
            )
        }
    }
}
