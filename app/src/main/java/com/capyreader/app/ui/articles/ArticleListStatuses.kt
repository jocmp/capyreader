package com.capyreader.app.ui.articles

import com.jocmp.capy.Article
import com.jocmp.capy.articles.StatusChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Read and starred changes made during one list session, for rows already paged in. */
class ArticleListStatuses {
    private val _statuses = MutableStateFlow<Map<String, ArticleListStatus>>(emptyMap())

    val statuses: StateFlow<Map<String, ArticleListStatus>> = _statuses.asStateFlow()

    fun apply(change: StatusChange) {
        _statuses.update { current ->
            current + change.articleIDs.associateWith { id ->
                val status = current[id] ?: ArticleListStatus()

                ArticleListStatus(
                    read = change.read ?: status.read,
                    starred = change.starred ?: status.starred,
                )
            }
        }
    }
}

data class ArticleListStatus(
    val read: Boolean? = null,
    val starred: Boolean? = null,
)

fun Article.withStatus(status: ArticleListStatus?): Article {
    status ?: return this

    val read = status.read ?: read
    val starred = status.starred ?: starred

    if (read == this.read && starred == this.starred) {
        return this
    }

    return copy(read = read, starred = starred)
}
