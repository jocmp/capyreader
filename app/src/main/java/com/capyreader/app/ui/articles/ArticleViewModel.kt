package com.capyreader.app.ui.articles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.capyreader.app.ui.Route
import com.jocmp.capy.Account

class ArticleViewModel(
    handle: SavedStateHandle,
    account: Account,
) : ViewModel() {
    val id = handle.toRoute<Route.Article>().id

    val article = account.findArticle(id)
}