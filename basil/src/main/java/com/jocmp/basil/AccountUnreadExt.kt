package com.jocmp.basil

import com.jocmp.basil.persistence.ArticleRecords

val Account.unreadCounts: Map<String, Long>
    get() = articles.countUnread()
