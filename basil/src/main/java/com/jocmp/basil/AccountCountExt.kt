package com.jocmp.basil

fun Account.countAll(status: ArticleStatus) = articleRecords.countAll(status)
