package com.capyreader.app.ui.articles.index

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.AnimatedPaneScope
import androidx.compose.material3.adaptive.layout.ExtendedPaneScaffoldPaneScope
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.PaneScaffoldValue
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import com.jocmp.capy.ArticleFilter

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun IndexScaffold(
    scaffoldNavigator: ThreePaneScaffoldNavigator<ArticleFilter>,
    listPane: @Composable () -> Unit,
    detailPane: @Composable () -> Unit,
) {
    ListDetailPaneScaffold(
        directive = scaffoldNavigator.scaffoldDirective,
        value = scaffoldNavigator.scaffoldValue,
        listPane = {
            IndexPane {
                Row {
                    listPane()
                }
            }
        },
        detailPane = {
            IndexPane {
                detailPane()
            }
        }
    )
}

@ExperimentalMaterial3AdaptiveApi
@Composable
private fun <S, T : PaneScaffoldValue<S>> ExtendedPaneScaffoldPaneScope<S, T>.IndexPane(content: (@Composable AnimatedPaneScope.() -> Unit)) {
    AnimatedPane(
        enterTransition = fadeIn(),
        exitTransition = fadeOut(),
        content = content,
    )
}