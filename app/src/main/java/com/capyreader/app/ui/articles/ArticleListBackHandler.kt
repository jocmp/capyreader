package com.capyreader.app.ui.articles

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.BackAction
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Folder
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject

@Composable
fun ArticleListBackHandler(
    filter: ArticleFilter,
    onRequestFilter: () -> Unit,
    onRequestFolder: (folder: Folder) -> Unit,
    appPreferences: AppPreferences = koinInject(),
    closeDrawer: () -> Unit,
    toggleDrawer: () -> Unit,
    enabled: Boolean,
    isDrawerOpen: Boolean,
) {
    val backAction by appPreferences.settings.map { it.backAction }.collectAsState(BackAction.default)

    if (!enabled) {
        return
    }

    BackHandler(backAction == BackAction.OPEN_DRAWER) {
        toggleDrawer()
    }

    BackHandler(backAction != BackAction.OPEN_DRAWER && isDrawerOpen) {
        closeDrawer()
    }

    BackHandler(backAction == BackAction.NAVIGATE_TO_PARENT && filter !is ArticleFilter.Articles) {
        when(filter) {
            is ArticleFilter.Feeds -> {
                val folderTitle = filter.folderTitle
                if (folderTitle != null) {
                    onRequestFolder(Folder(folderTitle))
                } else {
                    onRequestFilter()
                }
            }
            else -> onRequestFilter()
        }
    }
}
