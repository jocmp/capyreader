package com.capyreader.app.ui.articles.feeds.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capyreader.app.ui.components.DialogCard
import com.jocmp.capy.EditFeedFormEntry
import com.jocmp.capy.Feed
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditFeedDialog(
    feed: Feed,
    isOpen: Boolean,
    form: EditFeedViewModel = koinViewModel(),
    onDismiss: () -> Unit
) {
    val folders by form.folders.collectAsStateWithLifecycle(emptyList())
    val coroutineScope = rememberCoroutineScope()

    fun submit(entry: EditFeedFormEntry) {
        coroutineScope.launch {
            onDismiss()

            form.submit(entry)
        }
    }

    if (isOpen) {
        Dialog(onDismissRequest = onDismiss) {
            DialogCard {
                EditFeedView(
                    feed = feed,
                    folders = folders,
                    showMultiselect = form.showMultiselect,
                    onSubmit = ::submit,
                    onCancel = onDismiss
                )
            }
        }
    }
}
