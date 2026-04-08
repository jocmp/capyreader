package com.jocmp.capy

import java.time.LocalDate
import java.time.ZonedDateTime

data class FeedStats(
    val feed: Feed,
    val dailyCounts: List<DailyCount>,
    val chartDays: Long,
    val volume: Int,
    val latestArticleAt: ZonedDateTime?,
)

data class DailyCount(val day: LocalDate, val count: Int)
