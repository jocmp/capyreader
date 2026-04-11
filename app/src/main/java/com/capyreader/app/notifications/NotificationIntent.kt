package com.capyreader.app.notifications

import com.jocmp.capy.ArticleFilter

data class NotificationIntent(
    val articleID: String? = null,
    val filter: ArticleFilter? = null,
)
