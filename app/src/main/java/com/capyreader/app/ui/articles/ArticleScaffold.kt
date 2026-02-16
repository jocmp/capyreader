package com.capyreader.app.ui.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.components.CapyAnimatedPane
import com.capyreader.app.ui.isCompact
import com.capyreader.app.ui.theme.CapyTheme

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ArticleScaffold(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    scaffoldNavigator: ThreePaneScaffoldNavigator<Any> = rememberListDetailPaneScaffoldNavigator(),
    paneExpansion: ArticlePaneExpansion = rememberArticlePaneExpansion(),
    drawerPane: @Composable () -> Unit,
    listPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit,
) {
    val enableGesture = drawerState.isOpen ||
            isCompact() && scaffoldNavigator.currentDestination?.pane != ThreePaneScaffoldRole.Primary

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = enableGesture,
        drawerContent = {
            ModalDrawerSheet {
                drawerPane()
            }
        },
    ) {
        ListDetailPaneScaffold(
            directive = scaffoldNavigator.scaffoldDirective,
            scaffoldState = scaffoldNavigator.scaffoldState,
            paneExpansionDragHandle = { state ->
                val interactionSource = remember { MutableInteractionSource() }
                VerticalDragHandle(
                    modifier = Modifier.paneExpansionDraggable(
                        state,
                        LocalMinimumInteractiveComponentSize.current,
                        interactionSource,
                    ),
                    interactionSource = interactionSource,
                )
            },
            paneExpansionState = paneExpansion.state,
            listPane = {
                CapyAnimatedPane {
                    listPane()
                }
            },
            detailPane = {
                CapyAnimatedPane {
                    detailPane()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun ArticlesLayoutPreview() {
    CapyTheme {
        ArticleScaffold(
            drawerPane = {
                Text("List here!")
            },
            listPane = {
                Surface(
                    Modifier
                        .background(Color.Cyan)
                        .fillMaxSize()
                ) {
                    Text("Index list here...")
                }
            },
            detailPane = {
                Text("Detail!")
            }
        )
    }
}
