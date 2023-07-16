package com.jocmp.basilreader.ui.folders

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import com.jocmp.basilreader.lib.Async
import com.jocmp.basilreader.ui.get
import com.jocmp.feedbinclient.Subscriptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FoldersLayout() {
    when (val data = useFolders()) {
        is Async.Success -> FoldersList(data())
        is Async.Failure -> Text("Failure")
        else -> Text("Loading")
    }
}

@Composable
fun useFolders(): Async<List<Folder>> {
    val state = produceState<Async<List<Folder>>>(initialValue = Async.Uninitialized) {
        withContext(Dispatchers.IO) {
            value = get<Subscriptions>().all().fold(
                onSuccess = { results ->
                    val folders = results.map { subscription ->
                        Folder(
                            id = subscription.id.toString(),
                            name = subscription.feed_id.toString()
                        )
                    }
                    Async.Success(folders)
                },
                onFailure = { Async.Failure(it) }
            )
        }
    }
    return state.value
}