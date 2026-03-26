package com.capyreader.app.ui.articles

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface ArticleNavKey : NavKey {
    @Serializable
    data object List : ArticleNavKey

    @Serializable
    data object Detail : ArticleNavKey
}
