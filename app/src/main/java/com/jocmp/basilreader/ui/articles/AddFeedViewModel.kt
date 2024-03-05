package com.jocmp.basilreader.ui.articles

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.basil.AccountManager
import com.jocmp.basil.accounts.AddFeedResult
import com.jocmp.basil.accounts.FeedOption
import com.jocmp.basilreader.common.AppPreferences
import kotlinx.coroutines.launch

class AddFeedViewModel(
    accountManager: AccountManager,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    private val account = accountManager.findByID(appPreferences.accountID.get())!!
    private val _result = mutableStateOf<AddFeedResult?>(null)
    private val _loading = mutableStateOf(false)

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

    fun addFeed(
        url: String,
        onComplete: () -> Unit,
    ) {
        viewModelScope.launch {
            _loading.value = true
            val result = account.addFeed(url).getOrNull() ?: return@launch


            when (result) {
                is AddFeedResult.MultipleChoices -> {
                    _loading.value = false
                    _result.value = result
                }
                is AddFeedResult.Success -> onComplete()
            }
        }
    }
}
