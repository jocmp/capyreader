package com.capyreader.app.ui.articles.detail

import android.content.Context
import com.capyreader.app.R
import com.jocmp.capy.Article
import com.jocmp.capy.common.toDeviceDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun Article.byline(context: Context): String {
    val deviceDateTime = publishedAt.toDeviceDateTime()
    val date = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(deviceDateTime)
    val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(deviceDateTime)

    return if (!author.isNullOrBlank()) {
        context.getString(R.string.article_byline, date, time, author)
    } else {
        context.getString(R.string.article_byline_date_only, date, time)
    }
}
