package com.capyreader.app.ui.articles.list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.preferences.BadgeStyle
import com.capyreader.app.ui.LocalBadgeStyle
import com.capyreader.app.ui.articles.feeds.LocalSavedSearchActions
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.common.launchUI

@Composable
fun SavedSearchActionMenu(
    expanded: Boolean,
    savedSearch: SavedSearch,
    onDismissMenuRequest: () -> Unit,
) {
    val actions = LocalSavedSearchActions.current
    val scope = rememberCoroutineScope()

    fun onToggleUnreadBadge() {
        onDismissMenuRequest()
        scope.launchUI {
            actions.toggleUnreadBadge(savedSearch.id, !savedSearch.showUnreadBadge)
        }
    }

    if (LocalBadgeStyle.current != BadgeStyle.SIMPLE) {
        return
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissMenuRequest,
    ) {
        val trailingIcon = @Composable {
            if (savedSearch.showUnreadBadge) {
                Icon(Icons.Rounded.CheckBox, contentDescription = null)
            } else {
                Icon(Icons.Rounded.CheckBoxOutlineBlank, contentDescription = null)
            }
        }
        DropdownMenuItem(
            trailingIcon = trailingIcon,
            text = {
                Text(stringResource(R.string.show_unread_badge))
            },
            onClick = {
                onToggleUnreadBadge()
            }
        )
    }
}
