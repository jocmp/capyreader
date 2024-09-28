package com.capyreader.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.jocmp.capy.articles.UnreadSortOrder

@Composable
fun UnreadSortOrderSelect(
    selected: UnreadSortOrder,
    update: (UnreadSortOrder) -> Unit = {},
) {
    PreferenceSelect(
        selected = selected,
        update = update,
        options = UnreadSortOrder.entries,
        optionText = { stringResource(translationKey(it)) },
        label = R.string.article_list_unread_sort_title
    )
}

private fun translationKey(sortOrder: UnreadSortOrder) =
    when (sortOrder) {
        UnreadSortOrder.NEWEST_FIRST -> R.string.article_list_unread_sort_newest_first
        UnreadSortOrder.OLDEST_FIRST -> R.string.article_list_unread_sort_oldest_first
    }
