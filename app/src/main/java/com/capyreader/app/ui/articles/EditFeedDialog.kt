package com.capyreader.app.ui.articles

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jocmp.capy.EditFeedFormEntry
import com.jocmp.capy.Feed
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@Composable
fun EditFeedDialog(
    feed: Feed,
    form: EditFeedViewModel = koinViewModel(),
    onSubmit: () -> Unit,
    onFailure: () -> Unit,
    onCancel: () -> Unit
) {
    val allFolders by form.folders.collectAsStateWithLifecycle(emptyList())
    val folders = remember(UUID.randomUUID()) {
        allFolders
    }
    val submit = { entry: EditFeedFormEntry ->
        form.submit(entry, onSubmit, onFailure)
    }

    Dialog(onDismissRequest = onCancel) {
        Card {
            EditFeedView(
                feed = feed,
                folders = folders,
                showMultiselect = form.showMultiselect,
                onSubmit = submit,
                onCancel = onCancel
            )
        }
    }
}
