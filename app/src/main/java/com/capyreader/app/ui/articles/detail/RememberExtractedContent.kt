package com.capyreader.app.ui.articles.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
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
    account: Account = koinInject(),
    onComplete: (content: ExtractedContent) -> Unit
): ExtractedContentState {
    if (article == null) {
        return ExtractedContentState()
    }

    val scope = rememberCoroutineScope()

    val (extractedContent, setExtractedContent) = rememberSaveable(
        article.id,
        stateSaver = ExtractedArticleSaver,
    ) {
        mutableStateOf(ExtractedContent())
    }

    fun fetch() {
        setExtractedContent(ExtractedContent(requestShow = true, value = Async.Loading))

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
    }

    return ExtractedContentState(
        content = extractedContent,
        reset = ::reset,
        fetch = ::fetch
    )
}

val ExtractedArticleSaver: Saver<ExtractedContent, Any> = run {
    val showExtractedContent = "showExtractedContent"
    val extractedContent = "extractedContent"

    mapSaver(
        save = {
            mapOf(
                showExtractedContent to it.requestShow,
                extractedContent to it.value()
            )
        },
        restore = {
            val requestShow = (it[showExtractedContent] as Boolean?) ?: false
            val content = it[extractedContent] as? String

            val value = if (content != null) {
                Async.Success(content)
            } else {
                Async.Uninitialized
            }

            ExtractedContent(requestShow = requestShow, value = value)
        }
    )
}
