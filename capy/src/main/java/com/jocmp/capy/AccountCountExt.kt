package com.jocmp.capy

suspend fun Account.countToday(status: ArticleStatus) = articleRecords.countToday(status)
