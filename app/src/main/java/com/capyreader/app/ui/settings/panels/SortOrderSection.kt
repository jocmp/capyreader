package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.RowItem
import com.capyreader.app.ui.articles.ArticleStatusIcon
import com.capyreader.app.ui.articles.iconSize
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.navigationTitle
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.articles.SortOrder

@Composable
fun SortOrderSection(
    sortOrders: Map<ArticleStatus, SortOrder>,
    update: (status: ArticleStatus, sortOrder: SortOrder) -> Unit,
) {
    FormSection(title = stringResource(R.string.article_list_sort_title)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ArticleStatus.entries.forEach { status ->
                RowItem {
                    SortOrderRow(
                        status = status,
                        selected = sortOrders.getValue(status),
                        update = { update(status, it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SortOrderRow(
    status: ArticleStatus,
    selected: SortOrder,
    update: (SortOrder) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(24.dp),
            ) {
                ArticleStatusIcon(
                    status = status,
                    modifier = Modifier.size(iconSize(status)),
                )
            }
            Text(text = stringResource(status.navigationTitle))
        }
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SortOrder.entries.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == selected,
                    onClick = { update(option) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = SortOrder.entries.size,
                    ),
                    label = { Text(text = stringResource(translationKey(option))) },
                )
            }
        }
    }
}

private fun translationKey(sortOrder: SortOrder) =
    when (sortOrder) {
        SortOrder.NEWEST_FIRST -> R.string.article_list_sort_newest_first
        SortOrder.OLDEST_FIRST -> R.string.article_list_sort_oldest_first
    }

@Preview
@Composable
private fun SortOrderSectionPreview() {
    CapyTheme {
        Surface {
            SortOrderSection(
                sortOrders = mapOf(
                    ArticleStatus.ALL to SortOrder.NEWEST_FIRST,
                    ArticleStatus.UNREAD to SortOrder.OLDEST_FIRST,
                    ArticleStatus.STARRED to SortOrder.NEWEST_FIRST,
                ),
                update = { _, _ -> },
            )
        }
    }
}
