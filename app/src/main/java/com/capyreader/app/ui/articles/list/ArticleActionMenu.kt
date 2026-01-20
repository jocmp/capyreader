package com.capyreader.app.ui.articles.list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.shareLink
import com.capyreader.app.ui.articles.ArticleActions
import com.capyreader.app.ui.components.ArticleAction
import com.capyreader.app.ui.components.buildCopyToClipboard
import com.capyreader.app.ui.components.readAction
import com.capyreader.app.ui.components.starAction
import com.jocmp.capy.Article
import com.jocmp.capy.MarkRead
import com.jocmp.capy.MarkRead.After
import com.jocmp.capy.MarkRead.Before

@Composable
fun ArticleActionMenu(
    expanded: Boolean,
    article: Article,
    index: Int,
    unreadCount: Long,
    articleActions: ArticleActions,
    showLabels: Boolean = false,
    onMarkAllRead: (range: MarkRead) -> Unit = {},
    onOpenLabels: () -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismissRequest() },
        offset = DpOffset(x = 16.dp, y = 0.dp)
    ) {
        ToggleStarMenuItem(onDismissRequest, article, articleActions)
        ToggleReadMenuItem(onDismissRequest, article, articleActions)
        if (showLabels) {
            LabelMenuItem(onDismissRequest, onOpenLabels)
        }
        if (unreadCount > 0) {
            if (index > 0) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.icon_rounded_arrow_upward),
                            contentDescription = null
                        )
                    },
                    text = { Text(stringResource(R.string.article_actions_mark_after_as_read)) },
                    onClick = { onMarkAllRead(After(article.id)) },
                )
            }
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.icon_rounded_arrow_downward),
                        contentDescription = null
                    )
                },
                text = { Text(stringResource(R.string.article_actions_mark_below_as_read)) },
                onClick = { onMarkAllRead(Before(article.id)) },
            )
        }
        CopyLinkMenuItem(onDismissRequest, article)
        ShareLinkMenuItem(onDismissRequest, article)
    }
}

@Composable
private fun LabelMenuItem(
    onDismissRequest: () -> Unit,
    onOpenLabels: () -> Unit,
) {
    DropdownMenuItem(
        leadingIcon = {
            Icon(
                Icons.AutoMirrored.Outlined.Label,
                contentDescription = null
            )
        },
        text = { Text(stringResource(R.string.freshrss_article_actions_label)) },
        onClick = {
            onDismissRequest()
            onOpenLabels()
        },
    )
}

@Composable
private fun CopyLinkMenuItem(onDismissRequest: () -> Unit, article: Article) {
    val url = article.url?.toString() ?: return

    val copyToClipboard = buildCopyToClipboard(url)

    DropdownMenuItem(
        leadingIcon = {
            Icon(
                Icons.Rounded.ContentCopy,
                contentDescription = null
            )
        },
        text = { Text(stringResource(R.string.article_actions_copy_link)) },
        onClick = {
            copyToClipboard()
            onDismissRequest()
        },
    )
}

@Composable
private fun ShareLinkMenuItem(onDismissRequest: () -> Unit, article: Article) {
    val url = article.url?.toString() ?: return

    val context = LocalContext.current

    DropdownMenuItem(
        leadingIcon = {
            Icon(
                Icons.Rounded.Share,
                contentDescription = null
            )
        },
        text = { Text(stringResource(R.string.article_share)) },
        onClick = {
            context.shareLink(url = url, article.title)
            onDismissRequest()
        },
    )
}

@Composable
private fun ToggleReadMenuItem(
    onDismissRequest: () -> Unit,
    article: Article,
    articleActions: ArticleActions
) {
    val action = readAction(article, articleActions)
    ToggleActionMenuItem(onDismissRequest, action)
}

@Composable
private fun ToggleStarMenuItem(
    onDismissRequest: () -> Unit,
    article: Article,
    articleActions: ArticleActions
) {
    val action = starAction(article, articleActions)
    ToggleActionMenuItem(onDismissRequest, action)
}

@Composable
private fun ToggleActionMenuItem(
    onDismissRequest: () -> Unit,
    action: ArticleAction,
) {
    DropdownMenuItem(
        leadingIcon = {
            Icon(
                painterResource(action.icon),
                contentDescription = null
            )
        },
        text = { Text(stringResource(action.translationKey)) },
        onClick = {
            onDismissRequest()
            action.commit()
        },
    )
}
