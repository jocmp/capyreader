package com.capyreader.app.ui.articles

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capyreader.app.Notifications
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
                onSubmit = submit,
                onCancel = onCancel
            )
        }
    }
}
