package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.common.withIOContext
import com.jocmp.capy.logging.CapyLog
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FullContentFetcher(
    article: Article,
    private val onError: (error: Throwable) -> Unit
) : KoinComponent {
    private val account: Account by inject()
    private val appPreferences: AppPreferences by inject()
    var article = mutableStateOf(article)

    suspend fun fetchSticky() {
        if (enableStickyFullContent && article.value.enableStickyFullContent) {
            article.value =
                article.value.copy(content = "", fullContent = Article.FullContentState.LOADING)

            fetch()
        } else {
            article.value = article.value.copy(
                content = article.value.defaultContent,
                fullContent = Article.FullContentState.NONE
            )
        }
    }

    suspend fun fetch() {
        withIOContext {
            article.value =
                article.value.copy(content = "", fullContent = Article.FullContentState.LOADING)

            account.fetchFullContent(article.value)
                .fold(
                    onSuccess = { value ->
                        article.value = article.value.copy(
                            content = value,
                            fullContent = Article.FullContentState.LOADED
                        )
                    },
                    onFailure = {
                        article.value = article.value.copy(
                            content = article.value.defaultContent,
                            fullContent = Article.FullContentState.ERROR
                        )

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

            if (enableStickyFullContent && !account.isFullContentEnabled(feedID = article.value.feedID)) {
                account.enableStickyContent(article.value.feedID)
            }
        }
    }

    fun reset() {
        this.article.value = article.value.copy(
            content = article.value.defaultContent,
            fullContent = Article.FullContentState.NONE
        )

        if (enableStickyFullContent) {
            account.disableStickyContent(article.value.feedID)
        }
    }

    private val enableStickyFullContent
        get() = appPreferences.enableStickyFullContent.get()
}

@Composable
fun rememberFullContent(article: Article, onError: (error: Throwable) -> Unit): FullContentFetcher {
    val fetcher = remember(article.id) { FullContentFetcher(article, onError) }

    LaunchedEffect(article.id) {
        fetcher.fetchSticky()
    }

    return fetcher
}
