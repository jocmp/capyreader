package com.jocmp.basil

fun Account.countAll(status: ArticleStatus): Map<String, Long> =
    articleRecords.countAll(status)
