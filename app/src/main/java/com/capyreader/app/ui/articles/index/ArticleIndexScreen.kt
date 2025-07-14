package com.capyreader.app.ui.articles.index

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.common.launchUI

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ArticleIndexScreen(
    onNavigateToSettings: () -> Unit,
) {
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<ArticleFilter>(
    )
    val coroutineScope = rememberCoroutineScope()

    IndexScaffold(
        scaffoldNavigator,
        listPane = {
            Scaffold { values ->
                Column(Modifier.padding(values)) {
                    Button(onClick = {
                        coroutineScope.launchUI {
                            scaffoldNavigator.navigateTo(
                                ThreePaneScaffoldRole.Primary,
                                ArticleFilter.default()
                            )
                        }
                    }) {
                        Text(ArticleFilter.default().toString())
                    }
                }
            }
        },
        detailPane = {
            Scaffold { values ->
                Column(Modifier.padding(values)) {
                    scaffoldNavigator.currentDestination?.contentKey?.let { filter ->
                        Text("Filter: $filter")
                    }
                }
            }
        }
    )
}