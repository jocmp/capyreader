package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.common.withIOContext
import com.jocmp.capy.logging.CapyLog
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class FullContent(
    val initialText: String = "",
    val initialStatus: Article.FullContentState = Article.FullContentState.NONE,
) : KoinComponent {
    private val account: Account by inject()
    private val appPreferences: AppPreferences by inject()
    var text by mutableStateOf(initialText)
    var status by mutableStateOf(initialStatus)

    suspend fun fetchSticky(article: Article, onError: (error: Throwable) -> Unit) {
        if (enableStickyFullContent && article.enableStickyFullContent) {
            fetch(article, onError)
        } else {
            text = article.defaultContent
            status = Article.FullContentState.NONE
        }
    }

    suspend fun fetch(article: Article, onError: (error: Throwable) -> Unit) {
        if (status == Article.FullContentState.LOADED) {
            return
        }

        withIOContext {
            text = ""
            status = Article.FullContentState.LOADING

            account.fetchFullContent(article)
                .fold(
                    onSuccess = { value ->
                        text = value
                        status = Article.FullContentState.LOADED
                    },
                    onFailure = {
                        text = article.defaultContent
                        status = Article.FullContentState.ERROR

                        CapyLog.warn(
                            "full_content",
                            mapOf(
                                "error_type" to it::class.simpleName,
                                "error_message" to it.message
                            )
                        )

                        onError(it)
                    }
                )

            if (enableStickyFullContent && !account.isFullContentEnabled(feedID = article.feedID)) {
                account.enableStickyContent(article.feedID)
            }
        }
    }

    fun reset(article: Article) {
        text = article.defaultContent
        status = Article.FullContentState.NONE

        if (enableStickyFullContent) {
            account.disableStickyContent(article.feedID)
        }
    }

    private val enableStickyFullContent
        get() = appPreferences.enableStickyFullContent.get()

    companion object {
        val Saver: Saver<FullContent, *> =
            listSaver(
                save = { listOf(it.text, it.status.toString()) },
                restore = {
                    FullContent(
                        initialText = it[0],
                        initialStatus = Article.FullContentState.valueOf(it[1]),
                    )
                }
            )
    }
}

@Composable
fun rememberFullContent(article: Article, onError: (error: Throwable) -> Unit): FullContent {
    val fetcher = rememberSaveable(
        article.id,
        saver = FullContent.Saver
    ) {
        FullContent(article.content)
    }

    LaunchedEffect(article.id) {
        fetcher.fetchSticky(article, onError)
    }

    return fetcher
}
