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
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.PaneExpansionState
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.components.CapyAnimatedPane
import com.capyreader.app.ui.isAtMostMedium
import com.capyreader.app.ui.isCompact
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private val ArticlePaneAnchors = buildList {
    add(PaneExpansionAnchor.Proportion(0f))
    (45..70 step 5).forEach { add(PaneExpansionAnchor.Proportion(it / 100f)) }
    add(PaneExpansionAnchor.Proportion(1f))
}

private val DetailFullscreenAnchor = PaneExpansionAnchor.Proportion(0f)

private const val DefaultAnchorIndex = 1

@Stable
class ArticlePaneExpansion(
    val state: PaneExpansionState,
    val isFullscreen: Boolean,
    private val lastAnchorIndex: Int,
    private val scope: kotlinx.coroutines.CoroutineScope,
) {
    fun toggleFullscreen() {
        scope.launch {
            if (isFullscreen) {
                state.animateTo(ArticlePaneAnchors[DefaultAnchorIndex])
            } else {
                state.animateTo(DetailFullscreenAnchor)
            }
        }
    }

    suspend fun restoreAnchor() {
        state.animateTo(ArticlePaneAnchors[lastAnchorIndex])
    }

    fun resetAnchor() {
        scope.launch {
            state.animateTo(ArticlePaneAnchors[DefaultAnchorIndex])
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberArticlePaneExpansion(
    appPreferences: AppPreferences = koinInject(),
): ArticlePaneExpansion {
    val savedIndex = appPreferences.paneExpansionIndex.get()
        .coerceIn(0, ArticlePaneAnchors.lastIndex - 1)

    val paneExpansionState = rememberPaneExpansionState(
        anchors = ArticlePaneAnchors,
        initialAnchoredIndex = savedIndex,
    )
    val compact = isCompact()
    val scope = rememberCoroutineScope()
    var lastAnchorIndex by rememberSaveable { mutableIntStateOf(savedIndex) }
    val listFullscreenIndex = ArticlePaneAnchors.lastIndex

    LaunchedEffect(paneExpansionState.currentAnchor) {
        CapyLog.debug("current_anchor", mapOf("value" to paneExpansionState.currentAnchor))
        val anchor = paneExpansionState.currentAnchor ?: return@LaunchedEffect
        val index = ArticlePaneAnchors.indexOf(anchor)

        if (index in 0 until listFullscreenIndex) {
            lastAnchorIndex = index
            appPreferences.paneExpansionIndex.set(index)
        }
    }

    val isFullscreen = !compact &&
            paneExpansionState.currentAnchor == DetailFullscreenAnchor

    return remember(paneExpansionState, isFullscreen, lastAnchorIndex, scope) {
        ArticlePaneExpansion(
            state = paneExpansionState,
            isFullscreen = isFullscreen,
            lastAnchorIndex = lastAnchorIndex,
            scope = scope,
        )
    }
}

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
            isAtMostMedium() && scaffoldNavigator.currentDestination?.pane != ThreePaneScaffoldRole.Primary

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
