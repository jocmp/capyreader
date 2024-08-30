package com.capyreader.app.ui.articles.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.capyreader.app.common.AppPreferences
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ExtractedContent
import com.jocmp.capy.common.Async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class ExtractedContentState(
    val content: ExtractedContent = ExtractedContent(),
    val fetch: () -> Unit = {},
    val reset: () -> Unit = {},
)

@Composable
fun rememberExtractedContent(
    article: Article?,
    appPreferences: AppPreferences = koinInject(),
    account: Account = koinInject(),
    onComplete: (content: ExtractedContent) -> Unit
): ExtractedContentState {
    if (article == null) {
        return ExtractedContentState()
    }

    val scope = rememberCoroutineScope()
    val enableStickyFullContent by appPreferences
        .enableStickyFullContent
        .stateIn(scope)
        .collectAsState()

    val requestShow = enableStickyFullContent && article.enableStickyFullContent

    val (extractedContent, setExtractedContent) = rememberSaveable(
        article.id,
        stateSaver = ExtractedArticleSaver,
    ) {
        mutableStateOf(ExtractedContent(requestShow = requestShow))
    }

    fun fetch() {
        setExtractedContent(ExtractedContent(requestShow = true, value = Async.Loading))

        scope.launch(Dispatchers.IO) {
            if (enableStickyFullContent && !article.enableStickyFullContent) {
                account.enableStickyContent(article.feedID)
            }
        }

        scope.launch(Dispatchers.IO) {
            val value = account.fetchFullContent(article).fold(
                onSuccess = { Async.Success(it) },
                onFailure = { Async.Failure(it) }
            )

            val content = extractedContent.copy(requestShow = true, value = value)
            setExtractedContent(content)
            onComplete(content)
        }
    }

    fun reset() {
        setExtractedContent(ExtractedContent(value = Async.Uninitialized, requestShow = false))

        scope.launch {
            if (enableStickyFullContent) {
                account.disableStickyContent(article.feedID)
            }
        }
    }

    return ExtractedContentState(
        content = extractedContent,
        reset = ::reset,
        fetch = ::fetch
    )
}

val ExtractedArticleSaver: Saver<ExtractedContent, Any> = run {
    val showExtractedContent = "showExtractedContent"
    val complete = "complete"

    mapSaver(
        save = {
            mapOf(
                showExtractedContent to it.requestShow,
                complete to (it.value is Async.Success),
            )
        },
        restore = {
            val requestShow = (it[showExtractedContent] as Boolean?) ?: false
            val wasComplete = (it[complete] as Boolean?) ?: false

            val value = if (wasComplete) {
                Async.Success(value = "")
            } else {
                Async.Uninitialized
            }

            ExtractedContent(requestShow = requestShow, value = value)
        }
    )
}
