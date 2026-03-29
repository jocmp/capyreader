package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Dialog
import com.capyreader.app.ui.components.DialogCard
import com.jocmp.capy.EditFolderFormEntry
import com.jocmp.capy.Folder
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun EditFolderDialog(
    folderTitle: String,
    isOpen: Boolean,
    viewModel: EditFolderViewModel = koinViewModel(),
    onDismiss: () -> Unit,
    completion: (result: Result<Folder>) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val submit = { entry: EditFolderFormEntry ->
        coroutineScope.launch {
            onDismiss()

            completion(viewModel.submit(entry))
        }
    }

    if (isOpen) {
        Dialog(onDismissRequest = onDismiss) {
            DialogCard {
                EditFolderView(
                    folderTitle = folderTitle,
                    onSubmit = {
                        submit(it)
                    },
                    onCancel = onDismiss
                )
            }
        }
    }
}
