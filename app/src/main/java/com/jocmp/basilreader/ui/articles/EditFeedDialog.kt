package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jocmp.basil.EditFeedFormEntry
import com.jocmp.basil.Feed
import com.jocmp.basil.Folder
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditFeedDialog(
    feed: Feed,
    folders: List<Folder>,
    form: EditFeedViewModel = koinViewModel(),
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    val submit = { entry: EditFeedFormEntry ->
        form.submit(entry, onSubmit)
    }

    Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            EditFeedView(
                feed = feed,
                folders = folders,
                onSubmit = submit,
                onCancel = onCancel
            )
        }
    }
}
