package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.common.shareLink
import com.capyreader.app.ui.components.LocalSnackbarHost
import com.jocmp.capy.common.launchUI

@Composable
fun AudioMenuButton(
    url: String,
    title: String? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = LocalSnackbarHost.current
    val startedMessage = stringResource(R.string.audio_download_started)
    val failureMessage = stringResource(R.string.audio_download_failure)
    var expanded by remember { mutableStateOf(false) }

    fun download() {
        val message = AudioDownloader
            .download(url = url, title = title, context = context)
            .fold(
                onSuccess = { startedMessage },
                onFailure = { failureMessage },
            )

        scope.launchUI {
            snackbar.showSnackbar(message)
        }
    }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(R.string.audio_more_options),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Rounded.Download, contentDescription = null)
                },
                text = { Text(stringResource(R.string.audio_download)) },
                onClick = {
                    expanded = false
                    download()
                },
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Rounded.Share, contentDescription = null)
                },
                text = { Text(stringResource(R.string.actions_share_link)) },
                onClick = {
                    expanded = false
                    context.shareLink(url = url, title = title.orEmpty())
                },
            )
        }
    }
}
