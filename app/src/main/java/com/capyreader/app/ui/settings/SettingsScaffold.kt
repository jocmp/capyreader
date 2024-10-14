package com.capyreader.app.ui.settings

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.calculateListDetailPaneScaffoldMotion
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.capyreader.app.ui.FadePaneMotion
import com.capyreader.app.ui.components.safeEdgePadding
import com.capyreader.app.ui.settings.panels.SettingsPanel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsScaffold(
    scaffoldNavigator: ThreePaneScaffoldNavigator<SettingsPanel>,
    listPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit,
) {
    val paneMotions = calculateListDetailPaneScaffoldMotion(scaffoldNavigator.scaffoldValue).copy(
        primaryPaneMotion = FadePaneMotion(),
        secondaryPaneMotion = FadePaneMotion()
    )

    ListDetailPaneScaffold(
        modifier = Modifier.safeEdgePadding(),
        paneMotions = paneMotions,
        directive = scaffoldNavigator.scaffoldDirective,
        value = scaffoldNavigator.scaffoldValue,
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
