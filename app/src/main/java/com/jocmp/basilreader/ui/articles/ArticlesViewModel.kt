package com.jocmp.basilreader.ui.articles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jocmp.basil.AccountManager

class ArticlesViewModel(savedStateHandle: SavedStateHandle, accountManager: AccountManager): ViewModel() {
    private val args = ArticleArgs(savedStateHandle)
    val account = accountManager.findByID(args.accountId)
}
