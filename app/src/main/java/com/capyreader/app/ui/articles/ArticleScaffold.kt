package com.capyreader.app.ui.articles

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.AnimatedPaneScope
import androidx.compose.material3.adaptive.layout.ExtendedPaneScaffoldPaneScope
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.PaneScaffoldValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.preferences.LayoutPreference
import com.capyreader.app.ui.isAtMostMedium
import com.capyreader.app.ui.isExpanded
import com.capyreader.app.ui.rememberLayoutPreference
import com.capyreader.app.ui.theme.CapyTheme

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ArticleScaffold(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    scaffoldNavigator: ThreePaneScaffoldNavigator<Any> = rememberArticleScaffoldNavigator(),
    drawerPane: @Composable () -> Unit,
    listPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit,
) {
    val layout = rememberLayoutPreference()

    val enableGesture = drawerState.isOpen ||
            isAtMostMedium() && scaffoldNavigator.currentDestination?.pane != ThreePaneScaffoldRole.Primary

    if (layout == LayoutPreference.SINGLE) {
        ListDetailPaneScaffold(
            directive = scaffoldNavigator.scaffoldDirective,
            value = scaffoldNavigator.scaffoldValue,
            listPane = {
                ArticlePane {
                    Row {
                        Column(
                            Modifier
                                .sizeIn(
                                    minWidth = 240.dp,
                                    maxWidth = 300.dp
                                )
                                .windowInsetsPadding(DrawerDefaults.windowInsets)
                        ) {
                            drawerPane()
                        }

                        listPane()
                    }
                }
            },
            detailPane = {
                ArticlePane {
                    detailPane()
                }
            }
        )
    } else {
        NavDrawer(
            drawerState = drawerState,
            gesturesEnabled = enableGesture,
            drawerContent = {
                drawerPane()
            },
        ) {
            ListDetailPaneScaffold(
                directive = scaffoldNavigator.scaffoldDirective,
                value = scaffoldNavigator.scaffoldValue,
                listPane = {
                    ArticlePane {
                        listPane()
                    }
                },
                detailPane = {
                    ArticlePane {
                        detailPane()
                    }
                }
            )
        }
    }
}

@Composable
fun NavDrawer(
    drawerState: DrawerState,
    gesturesEnabled: Boolean,
    drawerContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    if (isExpanded()) {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet {
                    drawerContent()
                }
            },
            content = content
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = gesturesEnabled,
            drawerContent = {
                ModalDrawerSheet {
                    drawerContent()
                }
            },
            content = content
        )
    }
}

@ExperimentalMaterial3AdaptiveApi
@Composable
private fun <S, T : PaneScaffoldValue<S>> ExtendedPaneScaffoldPaneScope<S, T>.ArticlePane(content: (@Composable AnimatedPaneScope.() -> Unit)) {
    AnimatedPane(
        enterTransition = fadeIn(),
        exitTransition = fadeOut(),
        content = content,
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberArticleScaffoldNavigator(): ThreePaneScaffoldNavigator<Any> {
    val layout = rememberLayoutPreference()

    if (layout == LayoutPreference.SINGLE) {
        return rememberListDetailPaneScaffoldNavigator(
            scaffoldDirective = PaneScaffoldDirective(
                maxHorizontalPartitions = 1,
                horizontalPartitionSpacerSize = 0.dp,
                maxVerticalPartitions = 1,
                verticalPartitionSpacerSize = 0.dp,
                defaultPanePreferredWidth = 360.dp,
                emptyList(),
            )
        )
    }
    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())

    return rememberListDetailPaneScaffoldNavigator(
        scaffoldDirective = directive.copy(horizontalPartitionSpacerSize = 0.dp)
    )
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
