package com.capyreader.app.ui.articles

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.theme.CapyTheme

/**
 * The article list pane wrapped in the navigation drawer. The list-detail two-pane layout is now
 * handled at the navigation layer by the Nav3 list-detail Scene, so this only owns the drawer.
 */
@Composable
fun ArticleScaffold(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    drawerPane: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                drawerPane()
            }
        },
    ) {
        content()
    }
}

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun ArticlesLayoutPreview() {
    CapyTheme {
        ArticleScaffold(
            drawerPane = {
                Text("List here!")
            },
            content = {
                Text("Index list here...")
            },
        )
    }
}
