package com.capyreader.ui

import com.jocmp.capy.ArticleStatus
import com.capyreader.R

val ArticleStatus.navigationTitle: Int
    get() = when (this) {
        ArticleStatus.ALL -> R.string.filter_all
        ArticleStatus.UNREAD -> R.string.filter_unread
        ArticleStatus.STARRED -> R.string.filter_starred
    }
