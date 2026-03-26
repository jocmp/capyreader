package com.capyreader.app.ui.articles

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.sync.Sync
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.launchUI
import com.jocmp.capy.common.withUIContext
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

class ArticleViewModel(
    private val account: Account,
    private val appPreferences: AppPreferences,
    private val application: Application,
    private val notificationHelper: NotificationHelper,
) : AndroidViewModel(application) {

    private var fullContentJob: Job? = null

    var article by mutableStateOf<Article?>(null)
        private set

    val canSaveArticleExternally = account.canSaveArticleExternally.stateIn(viewModelScope)

    val source = account.source

    fun clearArticle() {
        article = null
    }

    fun loadArticle(id: String, onLoad: (Article) -> Unit = {}) {
        viewModelScope.launchIO {
            val loaded = buildArticle(id) ?: return@launchIO
            article = loaded

            withUIContext { onLoad(loaded) }

            launchIO { markRead(id) }

            if (loaded.fullContent == Article.FullContentState.LOADING) {
                fullContentJob?.cancel()
                fullContentJob = viewModelScope.launchIO { fetchFullContent(loaded) }
            }
        }
    }

    fun toggleArticleRead() {
        val current = article ?: return

        viewModelScope.launch {
            if (current.read) markUnread(current.id) else markRead(current.id)
        }

        article = current.copy(read = !current.read)
    }

    fun toggleArticleStar() {
        val current = article ?: return

        viewModelScope.launch {
            if (current.starred) removeStar(current.id) else addStar(current.id)
        }

        article = current.copy(starred = !current.starred)
    }

    fun fetchFullContentAsync(target: Article? = article) {
        target ?: return

        viewModelScope.launchIO {
            if (enableStickyFullContent && !account.isFullContentEnabled(feedID = target.feedID)) {
                account.enableStickyContent(target.feedID)
            }

            article = target.copy(fullContent = Article.FullContentState.LOADING)
            article?.let { fetchFullContent(it) }
        }
    }

    fun resetFullContent() {
        val current = article ?: return

        article = current.copy(
            content = current.defaultContent,
            fullContent = Article.FullContentState.NONE
        )

        if (enableStickyFullContent) {
            viewModelScope.launch { account.disableStickyContent(current.feedID) }
        }
    }

    fun deletePage(articleID: String) {
        viewModelScope.launchIO { account.deletePage(articleID) }
    }

    fun saveArticleExternallyAsync(articleID: String, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launchIO {
            val result = account.saveArticleExternally(articleID)
            withUIContext { onComplete(result) }
        }
    }

    fun getArticleLabels(articleID: String?): Flow<List<String>> {
        articleID ?: return emptyFlow()
        return account.getArticleSavedSearches(articleID)
    }

    fun addLabelAsync(articleID: String, savedSearchID: String) {
        viewModelScope.launchIO { account.addSavedSearch(articleID, savedSearchID) }
    }

    fun removeLabelAsync(articleID: String, savedSearchID: String) {
        viewModelScope.launchIO { account.removeSavedSearch(articleID, savedSearchID) }
    }

    suspend fun createLabel(articleID: String, name: String): Result<String> {
        return account.createSavedSearch(name).fold(
            onSuccess = { labelID ->
                account.addSavedSearch(articleID, labelID).fold(
                    onSuccess = { Result.success(labelID) },
                    onFailure = { Result.failure(it) }
                )
            },
            onFailure = { Result.failure(it) }
        )
    }

    private suspend fun buildArticle(articleID: String): Article? {
        val found = account.findArticle(articleID = articleID) ?: return null
        val fullContent = if (enableStickyFullContent && found.enableStickyFullContent) {
            Article.FullContentState.LOADING
        } else {
            Article.FullContentState.NONE
        }
        return found.copy(
            read = true,
            content = if (fullContent == Article.FullContentState.LOADING) "" else found.defaultContent,
            fullContent = fullContent
        )
    }

    private suspend fun fetchFullContent(article: Article) {
        account.fetchFullContent(article).fold(
            onSuccess = { value ->
                if (this.article?.id == article.id) {
                    this.article = article.copy(
                        content = value,
                        fullContent = Article.FullContentState.LOADED
                    )
                }
            },
            onFailure = {
                if (this.article?.id != article.id) return
                this.article = article.copy(
                    content = article.defaultContent,
                    fullContent = Article.FullContentState.ERROR
                )
                CapyLog.warn(
                    "full_content",
                    mapOf(
                        "error_type" to it::class.simpleName,
                        "error_message" to it.message
                    )
                )
                viewModelScope.launchUI { context.showFullContentErrorToast(it) }
            }
        )
    }

    private suspend fun markRead(articleID: String) {
        account.markRead(articleID).onFailure { Sync.markReadAsync(listOf(articleID), context) }
        notificationHelper.dismissNotifications(listOf(articleID))
    }

    private suspend fun markUnread(articleID: String) {
        account.markUnread(articleID).onFailure { Sync.markUnreadAsync(articleID, context) }
    }

    private suspend fun addStar(articleID: String) {
        account.addStar(articleID).onFailure { Sync.addStarAsync(articleID, context) }
    }

    private suspend fun removeStar(articleID: String) {
        account.removeStar(articleID).onFailure { Sync.removeStarAsync(articleID, context) }
    }

    private val enableStickyFullContent: Boolean
        get() = appPreferences.enableStickyFullContent.get()

    private val context: Context
        get() = application.applicationContext
}
