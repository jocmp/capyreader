@file:OptIn(ExperimentalMaterial3Api::class)

package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.fixtures.ArticleSample
import com.capyreader.app.ui.fixtures.PreviewKoinApplication
import com.jocmp.capy.Article

@Composable
fun ArticleTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    actions: @Composable RowScope.() -> Unit = {},
    onClose: () -> Unit,
) {
    val (isStyleSheetOpen, setStyleSheetOpen) = rememberSaveable { mutableStateOf(false) }

    TopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            ArticleNavigationIcon(
                onClick = onClose
            )
        },
        title = {},
        actions = actions,
    )

    if (isStyleSheetOpen) {
        ModalBottomSheet(onDismissRequest = { setStyleSheetOpen(false) }) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 16.dp)
            ) {
                ArticleStylePicker()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ArticleTopBarPreview(@PreviewParameter(ArticleSample::class) article: Article) {
    PreviewKoinApplication {
        ArticleTopBar(
            scrollBehavior = enterAlwaysScrollBehavior(),
            onClose = {}
        )
    }
}
