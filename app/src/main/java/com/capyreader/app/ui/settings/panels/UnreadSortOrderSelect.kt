package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.ui.settings.PreferenceSelect
import com.jocmp.capy.articles.SortOrder

@Composable
fun SortOrderSelect(
    selected: SortOrder,
    update: (SortOrder) -> Unit = {},
) {
    PreferenceSelect(
        selected = selected,
        update = update,
        options = SortOrder.entries,
        optionText = { stringResource(translationKey(it)) },
        label = R.string.article_list_sort_title
    )
}

private fun translationKey(sortOrder: SortOrder) =
    when (sortOrder) {
        SortOrder.NEWEST_FIRST -> R.string.article_list_sort_newest_first
        SortOrder.OLDEST_FIRST -> R.string.article_list_sort_oldest_first
    }
