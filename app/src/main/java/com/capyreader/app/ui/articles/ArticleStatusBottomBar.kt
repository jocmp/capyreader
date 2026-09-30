package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.articles.detail.ArticleBarDefaults
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.ArticleStatus

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArticleStatusBottomBar(
    status: ArticleStatus,
    onSelectStatus: (status: ArticleStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = ArticleBarDefaults.FloatingToolbarBottomGap),
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
            modifier = Modifier.height(ArticleBarDefaults.FloatingToolbarHeight),
        ) {
            ArticleStatusBar(
                status = status,
                onSelectStatus = onSelectStatus,
            )
        }
    }
}

@Preview
@Composable
private fun ArticleStatusBottomBarPreview() {
    CapyTheme {
        ArticleStatusBottomBar(
            status = ArticleStatus.UNREAD,
            onSelectStatus = {},
        )
    }
}
