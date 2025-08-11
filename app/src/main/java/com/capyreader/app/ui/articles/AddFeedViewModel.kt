package com.capyreader.app.ui.articles

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.Account
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.FeedOption
import com.jocmp.capy.common.launchIO
import com.jocmp.capy.common.withUIContext
import com.jocmp.capy.preferences.getAndSet

class AddFeedViewModel(
    val account: Account,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    private val _result = mutableStateOf<AddFeedResult?>(null)
    private val _loading = mutableStateOf(false)

    val loading: Boolean
        get() = _loading.value

    val error: AddFeedResult.Error?
        get() = (_result.value as? AddFeedResult.Failure)?.error

    val feedChoices: List<FeedOption>
        get() {
            return _result.value?.let {
                when (it) {
                    is AddFeedResult.MultipleChoices -> it.choices
                    else -> emptyList()
                }
            } ?: emptyList()
        }

    fun addFeed(
        url: String,
        onComplete: (feed: Feed) -> Unit,
    ) {
        viewModelScope.launchIO {
            _loading.value = true

            val result = account.addFeed(url = url)
            _loading.value = false

            withUIContext {
                when (result) {
                    is AddFeedResult.Success -> onComplete(result.feed)
                    else -> _result.value = result
                }
            }
        }
    }

    fun selectFeed(id: String) {
        viewModelScope.launchIO {
            appPreferences.filter.getAndSet {
                ArticleFilter.Feeds(feedID = id, folderTitle = null, it.status)
            }
        }
    }
}
