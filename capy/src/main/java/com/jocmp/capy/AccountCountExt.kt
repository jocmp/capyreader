package com.jocmp.capy

fun Account.countAll(status: ArticleStatus) = articleRecords.countAll(status)

suspend fun Account.countToday(status: ArticleStatus) = articleRecords.countToday(status)
