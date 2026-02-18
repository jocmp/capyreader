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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GeneralSettingsViewModel(
    private val refreshScheduler: RefreshScheduler,
    val account: Account,
    private val appPreferences: AppPreferences
) : ViewModel() {
    val source = account.source

    var refreshInterval by mutableStateOf(refreshScheduler.refreshInterval)
        private set

    var autoDelete by mutableStateOf(account.preferences.autoDelete.defaultValue())
        private set

    var canOpenLinksInternally by mutableStateOf(appPreferences.openLinksInternally.defaultValue())
        private set

    var sortOrder by mutableStateOf(appPreferences.articleListOptions.sortOrder.defaultValue())
        private set

    var confirmMarkAllRead by mutableStateOf(appPreferences.articleListOptions.confirmMarkAllRead.defaultValue())
        private set

    var markReadOnScroll by mutableStateOf(appPreferences.articleListOptions.markReadOnScroll.defaultValue())
        private set

    var afterReadAll by mutableStateOf(appPreferences.articleListOptions.afterReadAllBehavior.defaultValue())
        private set

    var enableStickyFullContent by mutableStateOf(appPreferences.enableStickyFullContent.defaultValue())
        private set

    var showTodayFilter by mutableStateOf(appPreferences.showTodayFilter.defaultValue())
        private set

    val keywordBlocklist = account
        .preferences
        .keywordBlocklist
        .changes()
        .stateIn(viewModelScope, SharingStarted.Eagerly, account.preferences.keywordBlocklist.defaultValue())

    init {
        viewModelScope.launch {
            autoDelete = account.preferences.autoDelete.get()
            canOpenLinksInternally = appPreferences.openLinksInternally.get()
            sortOrder = appPreferences.articleListOptions.sortOrder.get()
            confirmMarkAllRead = appPreferences.articleListOptions.confirmMarkAllRead.get()
            markReadOnScroll = appPreferences.articleListOptions.markReadOnScroll.get()
            afterReadAll = appPreferences.articleListOptions.afterReadAllBehavior.get()
            enableStickyFullContent = appPreferences.enableStickyFullContent.get()
            showTodayFilter = appPreferences.showTodayFilter.get()
        }
    }

    fun updateRefreshInterval(interval: RefreshInterval) {
        refreshScheduler.update(interval)

        this.refreshInterval = interval
    }

    fun updateSortOrder(sort: SortOrder) {
        viewModelScope.launch { appPreferences.articleListOptions.sortOrder.set(sort) }

        this.sortOrder = sort
    }

    fun updateAutoDelete(autoDelete: AutoDelete) {
        viewModelScope.launch { account.preferences.autoDelete.set(autoDelete) }

        this.autoDelete = autoDelete
    }

    fun updateOpenLinksInternally(openLinksInternally: Boolean) {
        viewModelScope.launch { appPreferences.openLinksInternally.set(openLinksInternally) }

        this.canOpenLinksInternally = openLinksInternally
    }

    fun updateConfirmMarkAllRead(confirm: Boolean) {
        viewModelScope.launch { appPreferences.articleListOptions.confirmMarkAllRead.set(confirm) }

        confirmMarkAllRead = confirm
    }

    fun updateAfterReadAll(behavior: AfterReadAllBehavior) {
        viewModelScope.launch { appPreferences.articleListOptions.afterReadAllBehavior.set(behavior) }

        afterReadAll = behavior
    }

    fun updateStickyFullContent(enable: Boolean) {
        viewModelScope.launch { appPreferences.enableStickyFullContent.set(enable) }

        enableStickyFullContent = enable

        if (!enable) {
            viewModelScope.launch(Dispatchers.IO) {
                account.clearStickyFullContent()
            }
        }
    }

    fun updateMarkReadOnScroll(enable: Boolean) {
        viewModelScope.launch { appPreferences.articleListOptions.markReadOnScroll.set(enable) }

        markReadOnScroll = enable
    }

    fun clearAllArticles() {
        viewModelScope.launch {
            account.clearAllArticles()
        }
    }

    fun addBlockedKeyword(keyword: String) {
        viewModelScope.launch {
            account.preferences.keywordBlocklist.getAndSet { list ->
                list.toMutableSet().apply { add(keyword) }
            }
        }
    }

    fun removeBlockedKeyword(keyword: String) {
        viewModelScope.launch {
            account.preferences.keywordBlocklist.getAndSet { list ->
                list.toMutableSet().apply { remove(keyword) }
            }
        }
    }

    fun updateShowTodayFilter(show: Boolean) {
        viewModelScope.launch { appPreferences.showTodayFilter.set(show) }

        showTodayFilter = show
    }
}
