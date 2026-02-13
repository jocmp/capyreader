package com.capyreader.app.ui.articles.detail

import android.content.Context
import com.capyreader.app.R
import com.jocmp.capy.Article
import com.jocmp.capy.common.toDeviceDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun Article.byline(context: Context, showReadingTime: Boolean = false): String {
    val deviceDateTime = publishedAt.toDeviceDateTime()
    val date = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(deviceDateTime)
    val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(deviceDateTime)
    val minutes = readingTimeMinutes

    if (showReadingTime && minutes != null) {
        val readingTimeText = context.resources.getQuantityString(
            R.plurals.reading_time_minutes,
            minutes.toInt(),
            minutes.toInt()
        )

        return if (!author.isNullOrBlank()) {
            context.getString(R.string.article_byline_with_reading_time, date, time, author, readingTimeText)
        } else {
            context.getString(R.string.article_byline_date_only_with_reading_time, date, time, readingTimeText)
        }
    }

    return if (!author.isNullOrBlank()) {
        context.getString(R.string.article_byline, date, time, author)
    } else {
        context.getString(R.string.article_byline_date_only, date, time)
    }
}
