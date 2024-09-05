package com.jocmp.capy.common

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle
import java.time.temporal.ChronoField

internal object DateTimeFormatters {
    val LONG_MONTH_DATE_TIME_FORMATTER: DateTimeFormatter by lazy {
        val dow = mapOf(
            1L to "Mon",
            2L to "Tue",
            3L to "Wed",
            4L to "Thu",
            5L to "Fri",
            6L to "Sat",
            7L to "Sun",
        )

        val moy = mapOf(
            1L to "Jan",
            2L to "Feb",
            3L to "Mar",
            4L to "Apr",
            5L to "May",
            6L to "Jun",
            7L to "Jul",
            8L to "Aug",
            9L to "Sept",
            10L to "Oct",
            11L to "Nov",
            12L to "Dec",
        )

        DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .parseLenient()
            .optionalStart()
            .appendText(ChronoField.DAY_OF_WEEK, dow)
            .appendLiteral(", ")
            .optionalEnd()
            .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
            .appendLiteral(' ')
            .appendText(ChronoField.MONTH_OF_YEAR, moy)
            .appendLiteral(' ')
            .appendValue(ChronoField.YEAR, 4)
            .appendLiteral(' ')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .optionalEnd()
            .appendLiteral(' ')
            .appendOffset("+HHMM", "GMT")
            .toFormatter()
    }
}
