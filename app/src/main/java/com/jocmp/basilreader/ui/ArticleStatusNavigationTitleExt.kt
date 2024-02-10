package com.jocmp.basilreader.ui

import com.jocmp.basil.ArticleStatus
import com.jocmp.basilreader.R

val ArticleStatus.navigationTitle: Int
    get() = when (this) {
        ArticleStatus.ALL -> R.string.filter_all
        ArticleStatus.UNREAD -> R.string.filter_unread
        ArticleStatus.STARRED -> R.string.filter_starred
    }
