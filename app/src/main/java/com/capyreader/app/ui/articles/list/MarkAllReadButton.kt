package com.capyreader.app.ui.articles.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.ui.LocalUnreadCount
import com.capyreader.app.ui.articles.MarkReadPosition

@Composable
fun MarkAllReadButton(
    position: MarkReadPosition = MarkReadPosition.TOOLBAR,
) {
    val unreadCount = LocalUnreadCount.current
    val requestMarkAllRead = LocalMarkAllRead.current

    if (position == MarkReadPosition.FLOATING_ACTION_BUTTON) {
        AnimatedVisibility(
            visible = unreadCount > 0,
            enter = slideInVertically { it * 2 },
            exit = slideOutVertically { it * 2 }
        ) {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                onClick = { requestMarkAllRead() }
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = stringResource(R.string.action_mark_all_read)
                )
            }
        }
    } else {
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
}
