package com.capyreader.app.ui.settings.panels

import com.jocmp.capy.common.toDateTimeFromSeconds
import com.jocmp.capy.common.toDeviceDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

sealed interface LastRefreshed {
    data object Never : LastRefreshed

    data class Today(val time: String) : LastRefreshed

    data class Past(val date: String, val time: String) : LastRefreshed

    companion object {
        fun from(
            epochSeconds: Long,
            today: LocalDate = LocalDate.now(),
        ): LastRefreshed {
            if (epochSeconds == 0L) {
                return Never
            }

            val deviceDateTime = epochSeconds.toDateTimeFromSeconds.toDeviceDateTime()
            val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(deviceDateTime)

            return if (deviceDateTime.toLocalDate() == today) {
                Today(time = time)
            } else {
                val date = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(deviceDateTime)
                Past(date = date, time = time)
            }
        }
    }
}
