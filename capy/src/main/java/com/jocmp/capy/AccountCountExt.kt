package com.jocmp.capy

fun Account.countAll(status: ArticleStatus) = articleRecords.countAll(status)

fun Account.countToday(status: ArticleStatus) = articleRecords.countToday(status)
