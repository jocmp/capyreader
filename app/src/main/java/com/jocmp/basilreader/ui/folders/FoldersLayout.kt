package com.jocmp.basilreader.ui.folders

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.navigation.NavController
import com.jocmp.basilreader.Route
import com.jocmp.basilreader.lib.Async
import com.jocmp.basilreader.ui.get
import com.jocmp.feedbinclient.CredentialsManager
import com.jocmp.feedbinclient.Subscriptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FoldersLayout(navController: NavController) {
    val hasAccount = get<CredentialsManager>().hasAccount

    LaunchedEffect(hasAccount) {
        if (!hasAccount) {
            navController.navigate(Route.AuthDialog)
        }
    }

    when (val data = useFolders()) {
        is Async.Success -> FoldersList(data())
        is Async.Failure -> Text("Failure")
        else -> Column {}
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
                            name = subscription.title
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