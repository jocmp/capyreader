package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ArticleListScaffold(
    padding: PaddingValues,
    showOnboarding: Boolean,
    onboarding: @Composable () -> Unit,
    articles: @Composable () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        if (showOnboarding) {
            onboarding()
        } else {
            articles()
        }
    }
}
