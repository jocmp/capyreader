package com.capyreader.app.ui.articles.list

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.jocmp.capy.MarkRead
import com.jocmp.capy.MarkRead.After
import com.jocmp.capy.MarkRead.Before
import com.capyreader.app.R

@Composable
fun ArticleActionMenu(
    expanded: Boolean,
    articleID: String,
    onMarkAllRead: (range: MarkRead) -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismissRequest() },
        offset = DpOffset(x = 4.dp, y = 0.dp)
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.icon_rounded_arrow_upward),
                    contentDescription = null
                )
            },
            text = { Text(stringResource(R.string.article_actions_mark_after_as_read)) },
            onClick = { onMarkAllRead(After(articleID)) },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.icon_rounded_arrow_downward),
                    contentDescription = null
                )
            },
            text = { Text(stringResource(R.string.article_actions_mark_below_as_read)) },
            onClick = { onMarkAllRead(Before(articleID)) },
        )
    }
}

@Preview
@Composable
private fun ArticleActionMenuPreview() {
    ArticleActionMenu(
        expanded = true,
        articleID = "1234"
    )
}
