package com.jocmp.capyreader.ui.articles

import androidx.compose.runtime.mutableStateOf
import com.jocmp.capy.Account
import com.jocmp.capy.Feed
import com.jocmp.capy.accounts.AddFeedResult
import com.jocmp.capy.accounts.FeedOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddFeedStateHolder(
    val account: Account
) {
    private val _result = mutableStateOf<AddFeedResult?>(null)
    private val _loading = mutableStateOf(false)

    val loading: Boolean
        get() = _loading.value

    val error: AddFeedResult.AddFeedError?
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

    suspend fun addFeed(
        url: String,
        onComplete: (feed: Feed) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
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
