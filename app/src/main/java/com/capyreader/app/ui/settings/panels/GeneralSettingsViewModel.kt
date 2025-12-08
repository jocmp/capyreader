package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.preferences.AfterReadAllBehavior
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.refresher.RefreshScheduler
import com.jocmp.capy.Account
import com.jocmp.capy.accounts.AutoDelete
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.preferences.getAndSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeneralSettingsViewModel(
    private val refreshScheduler: RefreshScheduler,
    val account: Account,
    private val appPreferences: AppPreferences
) : ViewModel() {
    val source = account.source

    var refreshInterval by mutableStateOf(refreshScheduler.refreshInterval)
        private set

    var autoDelete by mutableStateOf(account.preferences.autoDelete.get())
        private set

    var canOpenLinksInternally by mutableStateOf(appPreferences.openLinksInternally.get())
        private set

    var sortOrder by mutableStateOf(appPreferences.articleListOptions.sortOrder.get())
        private set

    var confirmMarkAllRead by mutableStateOf(appPreferences.articleListOptions.confirmMarkAllRead.get())
        private set

    var markReadOnScroll by mutableStateOf(appPreferences.articleListOptions.markReadOnScroll.get())
        private set

    var afterReadAll by mutableStateOf(appPreferences.articleListOptions.afterReadAllBehavior.get())
        private set

    var enableStickyFullContent by mutableStateOf(appPreferences.enableStickyFullContent.get())
        private set

    var showTodayFilter by mutableStateOf(appPreferences.showTodayFilter.get())
        private set

    val keywordBlocklist = account
        .preferences
        .keywordBlocklist
        .stateIn(viewModelScope)

    fun updateRefreshInterval(interval: RefreshInterval) {
        refreshScheduler.update(interval)

        this.refreshInterval = interval
    }

    fun updateSortOrder(sort: SortOrder) {
        appPreferences.articleListOptions.sortOrder.set(sort)

        this.sortOrder = sort
    }

    fun updateAutoDelete(autoDelete: AutoDelete) {
        account.preferences.autoDelete.set(autoDelete)

        this.autoDelete = autoDelete
    }

    fun updateOpenLinksInternally(openLinksInternally: Boolean) {
        appPreferences.openLinksInternally.set(openLinksInternally)

        this.canOpenLinksInternally = openLinksInternally
    }

    fun updateConfirmMarkAllRead(confirm: Boolean) {
        appPreferences.articleListOptions.confirmMarkAllRead.set(confirm)

        confirmMarkAllRead = confirm
    }

    fun updateAfterReadAll(behavior: AfterReadAllBehavior) {
        appPreferences.articleListOptions.afterReadAllBehavior.set(behavior)

        afterReadAll = behavior
    }

    fun updateStickyFullContent(enable: Boolean) {
        appPreferences.enableStickyFullContent.set(enable)

        enableStickyFullContent = enable

        if (!enable) {
            viewModelScope.launch(Dispatchers.IO) {
                account.clearStickyFullContent()
            }
        }
    }

    fun updateMarkReadOnScroll(enable: Boolean) {
        appPreferences.articleListOptions.markReadOnScroll.set(enable)

        markReadOnScroll = enable
    }

    fun clearAllArticles() {
        viewModelScope.launch {
            account.clearAllArticles()
        }
    }

    fun addBlockedKeyword(keyword: String) {
        account.preferences.keywordBlocklist.getAndSet { list ->
            list.toMutableSet().apply { add(keyword) }
        }
    }

    fun removeBlockedKeyword(keyword: String) {
        account.preferences.keywordBlocklist.getAndSet { list ->
            list.toMutableSet().apply { remove(keyword) }
        }
    }

    fun updateShowTodayFilter(show: Boolean) {
        appPreferences.showTodayFilter.set(show)

        showTodayFilter = show
    }
}
