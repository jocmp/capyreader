package com.capyreader.app.ui.articles.detail

import android.content.Context
import com.capyreader.app.R
import com.jocmp.capy.Article
import com.jocmp.capy.common.DisplayTimeFormats
import com.jocmp.capy.common.toDeviceDateTime

fun Article.byline(
    context: Context,
    formats: DisplayTimeFormats,
): String {
    val deviceDateTime = publishedAt.toDeviceDateTime()
    val date = formats.longDate.format(deviceDateTime)
    val time = formats.time.format(deviceDateTime)

    return if (!author.isNullOrBlank()) {
        context.getString(R.string.article_byline, date, time, author)
    } else {
        context.getString(R.string.article_byline_date_only, date, time)
    }
}
