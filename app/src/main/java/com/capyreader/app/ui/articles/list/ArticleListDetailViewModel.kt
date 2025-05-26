package com.capyreader.app.ui.articles.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.capyreader.app.ui.Route
import com.capyreader.app.ui.articles.WrappedArticleFilter

class ArticleListDetailViewModel(handle: SavedStateHandle) : ViewModel() {
    val filter =
        handle.toRoute<Route.ArticleListDetail>(typeMap = WrappedArticleFilter.typeMap).filter
}
