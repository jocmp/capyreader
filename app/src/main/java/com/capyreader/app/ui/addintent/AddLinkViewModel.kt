package com.capyreader.app.ui.addintent

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
import okio.IOException
import java.net.UnknownHostException

class AddLinkViewModel(
    private val account: Account,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    // --- Add Feed state ---
    private val _feedResult = mutableStateOf<AddFeedResult?>(null)
    private val _feedLoading = mutableStateOf(false)

    val feedLoading: Boolean
        get() = _feedLoading.value

    val feedError: AddFeedResult.Error?
        get() = (_feedResult.value as? AddFeedResult.Failure)?.error

    val feedChoices: List<FeedOption>
        get() {
            val result = _feedResult.value ?: return emptyList()

            return when (result) {
                is AddFeedResult.MultipleChoices -> result.choices
                else -> emptyList()
            }
        }

    fun searchFeed(url: String) {
        viewModelScope.launchIO {
            _feedLoading.value = true

            val result = account.searchFeed(url)

            _feedLoading.value = false

            withUIContext {
                result.fold(
                    onSuccess = { feeds ->
                        val choices = feeds.map {
                            FeedOption(
                                feedURL = it.feedURL.toString(),
                                title = it.name
                            )
                        }
                        _feedResult.value = AddFeedResult.MultipleChoices(choices)
                    },
                    onFailure = { exception ->
                        _feedResult.value = when (exception) {
                            is UnknownHostException -> AddFeedResult.connectionError()
                            is IOException -> AddFeedResult.networkError()
                            else -> AddFeedResult.feedNotFound()
                        }
                    }
                )
            }
        }
    }

    fun addFeed(url: String, onComplete: (Feed) -> Unit) {
        viewModelScope.launchIO {
            _feedLoading.value = true

            val result = account.addFeed(url = url)
            _feedLoading.value = false

            withUIContext {
                when (result) {
                    is AddFeedResult.Success -> onComplete(result.feed)
                    else -> _feedResult.value = result
                }
            }
        }
    }

    fun selectFeed(id: String) {
        appPreferences.filter.getAndSet {
            ArticleFilter.Feeds(feedID = id, folderTitle = null, it.status)
        }
    }

    // --- Save Page state ---
    private val _pageLoading = mutableStateOf(false)
    private val _pageError = mutableStateOf<String?>(null)

    val pageLoading: Boolean
        get() = _pageLoading.value

    val pageError: String?
        get() = _pageError.value

    fun savePage(url: String, onComplete: () -> Unit) {
        viewModelScope.launchIO {
            _pageLoading.value = true
            _pageError.value = null

            account.createPage(url).fold(
                onSuccess = {
                    _pageLoading.value = false
                    withUIContext { onComplete() }
                },
                onFailure = { exception ->
                    _pageLoading.value = false
                    _pageError.value = if (exception is IOException) {
                        "network"
                    } else {
                        "generic"
                    }
                }
            )
        }
    }
}
