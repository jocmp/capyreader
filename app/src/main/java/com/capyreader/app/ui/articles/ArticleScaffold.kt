package com.capyreader.app.ui.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculateListDetailPaneScaffoldMotion
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.FadePaneMotion
import com.capyreader.app.ui.components.safeEdgePadding
import com.capyreader.app.ui.isAtMostMedium
import com.capyreader.app.ui.theme.CapyTheme

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ArticleScaffold(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    scaffoldNavigator: ThreePaneScaffoldNavigator<Any> = rememberListDetailPaneScaffoldNavigator(),
    drawerPane: @Composable () -> Unit,
    listPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit,
) {
    val enableGesture = drawerState.isOpen ||
            isAtMostMedium() && scaffoldNavigator.currentDestination?.pane != ThreePaneScaffoldRole.Primary

    val paneMotions = calculateListDetailPaneScaffoldMotion(scaffoldNavigator.scaffoldValue).copy(
        primaryPaneMotion = FadePaneMotion(),
        secondaryPaneMotion = FadePaneMotion()
    )

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
            modifier = Modifier.safeEdgePadding(),
            directive = scaffoldNavigator.scaffoldDirective,
            value = scaffoldNavigator.scaffoldValue,
            paneMotions = paneMotions,
            listPane = {
                AnimatedPane {
                    listPane()
                }
            },
            detailPane = {
                AnimatedPane {
                    detailPane()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(device = Devices.TABLET)
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
