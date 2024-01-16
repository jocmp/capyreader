package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.PaneScaffoldDirective
import androidx.compose.material3.adaptive.ThreePaneScaffoldState
import androidx.compose.material3.adaptive.calculateListDetailPaneScaffoldState
import androidx.compose.material3.adaptive.calculateStandardPaneScaffoldDirective
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jocmp.basilreader.ui.theme.BasilReaderTheme

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ArticleScaffold(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    listDetailState: ThreePaneScaffoldState = calculateListDetailPaneScaffoldState(
        currentPaneDestination = ListDetailPaneScaffoldRole.Extra,
        scaffoldDirective = calculateArticleDirective()
    ),
    drawerPane: @Composable () -> Unit,
    listPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                drawerPane()
            }
        },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            ListDetailPaneScaffold(
                scaffoldState = listDetailState,
                listPane = {
                    listPane()
                },
                detailPane = {
                    detailPane()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun calculateArticleDirective(): PaneScaffoldDirective {
    val calculated = calculateStandardPaneScaffoldDirective(currentWindowAdaptiveInfo())

    return copyDirectiveWithoutPadding(calculated)
}


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun copyDirectiveWithoutPadding(directive: PaneScaffoldDirective): PaneScaffoldDirective {
    return PaneScaffoldDirective(
        contentPadding = PaddingValues(0.dp),
        maxHorizontalPartitions = directive.maxHorizontalPartitions,
        horizontalPartitionSpacerSize = 0.dp,
        maxVerticalPartitions = directive.maxVerticalPartitions,
        verticalPartitionSpacerSize = directive.verticalPartitionSpacerSize,
        excludedBounds = directive.excludedBounds
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(device = Devices.TABLET)
@Composable
fun ArticlesLayoutPreview() {
    BasilReaderTheme {
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
