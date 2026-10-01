package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection

@Composable
fun ArticleListScaffold(
    padding: PaddingValues,
    showOnboarding: Boolean,
    onboarding: @Composable () -> Unit,
    articles: @Composable () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    Box(
        Modifier
            .fillMaxSize()
            .padding(
                start = padding.calculateStartPadding(layoutDirection),
                top = padding.calculateTopPadding(),
                end = padding.calculateEndPadding(layoutDirection),
            )
    ) {
        if (showOnboarding) {
            onboarding()
        } else {
            articles()
        }
    }
}
