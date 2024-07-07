package com.capyreader.ui.articles

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.capy.ArticleStatus
import com.capyreader.R

@Composable
fun ArticleFilterMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelect: (status: ArticleStatus) -> Unit
) {
    val checkedSelect = { status: ArticleStatus ->
        onSelect(status)
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismissRequest() },
        modifier = Modifier.widthIn(min = 200.dp)
    ) {
        DropdownMenuItem(
            leadingIcon = {
                ArticleStatusIcon(status = ArticleStatus.ALL)
            },
            text = { Text(stringResource(R.string.filter_all)) },
            onClick = { checkedSelect(ArticleStatus.ALL) },
        )
        DropdownMenuItem(
            leadingIcon = {
                ArticleStatusIcon(status = ArticleStatus.UNREAD)
            },
            text = { Text(stringResource(R.string.filter_unread)) },
            onClick = { checkedSelect(ArticleStatus.UNREAD) },
        )
        DropdownMenuItem(
            leadingIcon = {
                ArticleStatusIcon(status = ArticleStatus.STARRED)
            },
            text = { Text(stringResource(id = R.string.filter_starred)) },
            onClick = { checkedSelect(ArticleStatus.STARRED) },
        )
    }
}

@Preview
@Composable
fun ArticleFilterNavigationBarPreview() {
    ArticleFilterMenu(
        expanded = true,
        onDismissRequest = {},
        onSelect = {}
    )
}
