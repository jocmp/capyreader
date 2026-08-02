package com.capyreader.app.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.ArticleStatus

@Composable
fun ArticleStatusBottomBar(
    status: ArticleStatus,
    onSelectStatus: (status: ArticleStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
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
