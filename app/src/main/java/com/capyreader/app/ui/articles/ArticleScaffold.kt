package com.capyreader.app.ui.articles

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import com.capyreader.app.ui.isCompact

@Composable
fun ArticleScaffold(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    isDetailVisible: Boolean = false,
    drawerPane: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val enableGesture = drawerState.isOpen ||
            (isCompact() && !isDetailVisible)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = enableGesture,
        drawerContent = {
            ModalDrawerSheet {
                drawerPane()
            }
        },
        content = content,
    )
}
