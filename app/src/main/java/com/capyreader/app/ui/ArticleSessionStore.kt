package com.capyreader.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * A [ViewModelStoreOwner] shared by the [Route.Feeds] and [Route.ArticleList] entries so both
 * read one [com.capyreader.app.ui.articles.ArticleScreenViewModel]. It outlives either entry
 * (compact pops the list to show feeds) and survives configuration changes, and is cleared once
 * [active] goes false, e.g. after removing the account.
 */
@Composable
fun rememberArticleSessionOwner(active: Boolean): ViewModelStoreOwner {
    val holder = viewModel<ArticleSessionStoreHolder>()

    LaunchedEffect(active) {
        if (!active) {
            holder.viewModelStore.clear()
        }
    }

    return remember(holder) { holder }
}

class ArticleSessionStoreHolder : ViewModel(), ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()

    override fun onCleared() {
        viewModelStore.clear()
    }
}
