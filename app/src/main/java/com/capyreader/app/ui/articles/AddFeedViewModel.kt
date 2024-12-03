package com.capyreader.app.ui.articles

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.capy.Account
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.FeedOption
import com.jocmp.capy.common.launchIO

class AddFeedViewModel(
    val account: Account
): ViewModel() {
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

            when (result) {
                is AddFeedResult.Success -> onComplete(result.feed)
                else -> _result.value = result
            }
        }
    }
}
