package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.mutableStateOf
import com.jocmp.basil.Account
import com.jocmp.basil.Feed
import com.jocmp.basil.accounts.AddFeedResult
import com.jocmp.basil.accounts.FeedOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddFeedStateHolder(
    val account: Account
) {
    private val _result = mutableStateOf<AddFeedResult?>(null)
    private val _loading = mutableStateOf(false)
    private val _hasError = mutableStateOf(false)

    val hasError: Boolean
        get() = _hasError.value

    val loading: Boolean
        get() = _loading.value

    val feedChoices: List<FeedOption>
        get() {
            return _result.value?.let {
                when (it) {
                    is AddFeedResult.MultipleChoices -> it.choices
                    is AddFeedResult.Success -> emptyList()
                }
            } ?: emptyList()
        }

    suspend fun addFeed(
        url: String,
        onComplete: (feed: Feed) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            _loading.value = true
            _hasError.value = false

            val result = account.addFeed(url).getOrNull()
            _loading.value = false

            if (result == null) {
                _hasError.value = true
                return@withContext
            }

            when (result) {
                is AddFeedResult.MultipleChoices -> {
                    _loading.value = false
                    _result.value = result
                }

                is AddFeedResult.Success -> onComplete(result.feed)
            }
        }
    }
}
