package com.capyreader.app.ui

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf

/**
 * Bridges the navigation drawer between the window-level host (in [App], which owns the
 * [ModalNavigationDrawer] so its scrim covers the whole window — both list and detail panes) and
 * the article list entry, which owns the drawer's content and the ViewModel that drives it.
 *
 * The list entry calls [setContent] to publish its drawer pane upward, and reads [state] to open
 * and close the drawer. Null when no host is present (e.g. previews).
 */
@Stable
class AppDrawerController(
    val state: DrawerState,
    val setContent: (content: (@Composable () -> Unit)?) -> Unit,
)

val LocalAppDrawer = compositionLocalOf<AppDrawerController?> { null }
