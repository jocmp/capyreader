package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
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
import com.capyreader.app.ui.navigationTitle
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.articles.SortOrder
import org.koin.androidx.compose.koinViewModel

@Composable
fun SortOrderSetting(viewModel: GeneralSettingsViewModel = koinViewModel()) {
    SortOrderRows(
        sortOrders = viewModel.sortOrders,
        update = viewModel::updateSortOrder,
    )
}

@Composable
private fun SortOrderRows(
    sortOrders: Map<ArticleStatus, SortOrder>,
    update: (status: ArticleStatus, sortOrder: SortOrder) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SortOrderRow(
    status: ArticleStatus,
    selected: SortOrder,
    update: (SortOrder) -> Unit,
) {
    val options = SortOrder.entries

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
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
        Spacer(Modifier.width(12.dp))
        Text(text = stringResource(status.navigationTitle))
        Spacer(Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            options.forEachIndexed { index, option ->
                ToggleButton(
                    checked = option == selected,
                    onCheckedChange = { update(option) },
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                ) {
                    Text(text = stringResource(translationKey(option)))
                }
            }
        }
    }
}

private fun translationKey(sortOrder: SortOrder) =
    when (sortOrder) {
        SortOrder.NEWEST_FIRST -> R.string.article_list_sort_newest
        SortOrder.OLDEST_FIRST -> R.string.article_list_sort_oldest
    }

@Preview
@Composable
private fun SortOrderRowsPreview() {
    CapyTheme {
        Surface {
            SortOrderRows(
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
