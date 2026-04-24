package com.jocmp.capy.common

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class DisplayTimeFormats(
    val time: DateTimeFormatter,
    val shortDate: DateTimeFormatter,
    val longDate: DateTimeFormatter,
) {
    companion object {
        fun localized(): DisplayTimeFormats {
            return DisplayTimeFormats(
                time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT),
                shortDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM),
                longDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG),
            )
        }
    }
}
