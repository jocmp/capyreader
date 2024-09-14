package com.capyreader.app.ui.articles.detail

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.capyreader.app.common.ArticleRenderer
import com.capyreader.app.ui.components.WebView
import com.capyreader.app.ui.components.WebViewState
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ExtractedContent
import my.nanihadesuka.compose.ColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleView(
    article: Article,
    webViewState: WebViewState,
    renderer: ArticleRenderer = koinInject(),
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    enableBackHandler: Boolean = false
) {
    val articleID = article.id
    val templateColors = articleTemplateColors()
    val colors = templateColors.asMap()
    val scrollState = rememberSaveable(articleID, saver = ScrollState.Saver) {
        ScrollState(0)
    }
    val lastScrollY = rememberLastScrollY(articleID, scrollState = scrollState)
    val showTopBar = scrollState.value == 0 || scrollState.lastScrolledBackward
    val (initialized, setInitialized) = remember(articleID) { mutableStateOf(false) }

    fun render(extractedContent: ExtractedContent = ExtractedContent()): String {
        return renderer.render(
            article,
            extractedContent = extractedContent,
            colors = colors
        )
    }

    val extractedContentState = rememberExtractedContent(
        article = article,
        onComplete = { content ->
            webViewState.loadHtml(render(content))
        }
    )

    val extractedContent = extractedContentState.content

    fun update() {
        if (initialized) {
            return
        }

        if (extractedContent.requestShow) {
            extractedContentState.fetch()
        } else {
            webViewState.loadHtml(render())
        }

        setInitialized(true)
    }

    fun onToggleExtractContent() {
        if (extractedContent.isComplete) {
            webViewState.loadHtml(render())
            extractedContentState.reset()
        } else if (!extractedContent.isLoading) {
            extractedContentState.fetch()
        }
    }

    Scaffold { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            ColumnScrollbar(
                state = scrollState,
                settings = ScrollbarSettings.Default.copy(
                    thumbThickness = 4.dp,
                    hideDisplacement = 0.dp,
                    scrollbarPadding = 2.dp,
                    thumbSelectedColor = colorScheme.onSurfaceVariant,
                    thumbUnselectedColor = colorScheme.onSurfaceVariant
                )
            ) {
                Column(
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    Spacer(Modifier.height(TopAppBarDefaults.TopAppBarExpandedHeight))
                    WebView(
                        state = webViewState,
                        onUpdate = { update() },
                        onDispose = {
                            lastScrollY.intValue = scrollState.value
                        }
                    )
                }
            }
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

    ArticleStyleListener(webView = webViewState.webView)
}

@Composable
fun rememberLastScrollY(articleID: String, scrollState: ScrollState): MutableIntState {
    val lastScrollY = rememberSaveable(articleID) {
        mutableIntStateOf(0)
    }

    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue > 0 && lastScrollY.intValue > 0) {
            scrollState.scrollTo(lastScrollY.intValue)
            lastScrollY.intValue = 0
        }
    }

    return lastScrollY
}
