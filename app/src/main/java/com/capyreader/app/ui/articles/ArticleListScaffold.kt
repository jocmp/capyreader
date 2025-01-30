package com.capyreader.app.ui.articles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
    showSearch: Boolean,
    onboarding: @Composable () -> Unit,
    search: @Composable () -> Unit,
    articles: @Composable () -> Unit,
) {
    Box(
        Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        if (showOnboarding) {
            onboarding()
        } else {
            articles()
        }

        AnimatedVisibility(
            showSearch,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            search()
        }
    }
}
