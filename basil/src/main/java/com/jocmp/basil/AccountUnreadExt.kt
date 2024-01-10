package com.jocmp.basil

val Account.unreadCounts: Map<String, Long>
    get() = articleRecords.countUnread()
