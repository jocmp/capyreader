package com.capyreader.app.ui.articles.list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.ui.LocalUnreadCount

@Composable
fun MarkAllReadButton() {
    val unreadCount = LocalUnreadCount.current
    val requestMarkAllRead = LocalMarkAllRead.current

    IconButton(
        enabled = unreadCount > 0,
        onClick = { requestMarkAllRead() }
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = stringResource(R.string.action_mark_all_read)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MarkAllReadFloatingActionButton(modifier: Modifier = Modifier) {
    val requestMarkAllRead = LocalMarkAllRead.current

    FloatingToolbarDefaults.StandardFloatingActionButton(
        onClick = { requestMarkAllRead() },
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = stringResource(R.string.action_mark_all_read)
        )
    }
}
