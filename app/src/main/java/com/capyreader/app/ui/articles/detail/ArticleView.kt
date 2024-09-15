package com.capyreader.app.ui.articles.detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.WebViewNavigator
import com.capyreader.app.ui.components.rememberSaveableWebViewState
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ArticleRenderer
import com.jocmp.capy.articles.ExtractedContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleView(
    article: Article,
    webViewNavigator: WebViewNavigator,
    renderer: ArticleRenderer = koinInject(),
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    onNavigateToMedia: (url: String) -> Unit,
    enableBackHandler: Boolean = false
) {
    val articleID = article.id
    val templateColors = articleTemplateColors()
    val colors = templateColors.asMap()
    val webViewState = rememberSaveableWebViewState(key = articleID)
    val byline = article.byline(context = LocalContext.current)
    val showTopBar = webViewState.scrollValue == 0 || webViewState.lastScrolledBackward

    fun render(extractedContent: ExtractedContent = ExtractedContent()): String {
        return renderer.render(
            article,
            byline = byline,
            extractedContent = extractedContent,
            colors = colors
        )
    }

    val extractedContentState = rememberExtractedContent(
        article = article,
        onComplete = { content ->
            article.let {
                webViewNavigator.loadHtml(render(content))
            }
        }
    )

    val extractedContent = extractedContentState.content

    fun onToggleExtractContent() {
        if (extractedContent.isComplete) {
            webViewNavigator.loadHtml(render())
            extractedContentState.reset()
        } else if (!extractedContent.isLoading) {
            extractedContentState.fetch()
        }
    }

    Scaffold { innerPadding ->
        Box(
            Modifier.fillMaxSize()
        ) {
            WebView(
                state = webViewState,
                navigator = webViewNavigator,
                onNavigateToMedia = onNavigateToMedia,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            )
            AnimatedVisibility(
                visible = showTopBar,
                enter = fadeIn() + expandVertically(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ArticleTopBar(
                    article = article,
                    extractedContent = extractedContent,
                    onToggleExtractContent = ::onToggleExtractContent,
                    onToggleRead = onToggleRead,
                    onToggleStar = onToggleStar,
                    onClose = onBackPressed
                )
            }
        }
    }

    BackHandler(enableBackHandler) {
        onBackPressed()
    }

    LaunchedEffect(articleID) {
        launch(Dispatchers.IO) {
            if (extractedContent.requestShow) {
                extractedContentState.fetch()
            } else {
                webViewNavigator.loadHtml(render())
            }
        }
    }

    ArticleStyleListener(webView = webViewState.webView)
}
