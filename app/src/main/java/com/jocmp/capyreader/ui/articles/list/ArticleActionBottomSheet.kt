package com.jocmp.capyreader.ui.articles.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.capy.MarkRead
import com.jocmp.capy.MarkRead.After
import com.jocmp.capy.MarkRead.Before
import com.jocmp.capyreader.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleActionBottomSheet(
    articleID: String,
    onMarkAllRead: (range: MarkRead) -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(Modifier.padding(bottom = 32.dp)) {
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(R.drawable.icon_arrow_upward),
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(stringResource(R.string.article_actions_mark_after_as_read))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onMarkAllRead(After(articleID))
                    }
            )
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(R.drawable.icon_arrow_downward),
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(stringResource(R.string.article_actions_mark_below_as_read))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onMarkAllRead(Before(articleID))
                    }
            )
        }
    }
}

@Preview
@Composable
private fun ArticleActionMenuPreview() {
    ArticleActionBottomSheet(
        articleID = "1234"
    )
}
