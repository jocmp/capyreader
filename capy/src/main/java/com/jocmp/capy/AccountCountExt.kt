package com.jocmp.capy

fun Account.countAll(status: ArticleStatus) = articleRecords.countAll(status)
