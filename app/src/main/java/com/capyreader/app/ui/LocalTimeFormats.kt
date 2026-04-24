package com.capyreader.app.ui

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.jocmp.capy.common.DisplayTimeFormats
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val LocalTimeFormats = compositionLocalOf { DisplayTimeFormats.localized() }

@Composable
fun rememberDisplayTimeFormats(): DisplayTimeFormats {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        val time = if (DateFormat.is24HourFormat(context)) {
            DateTimeFormatter.ofPattern("HH:mm")
        } else {
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        }
        DisplayTimeFormats(
            time = time,
            shortDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM),
            longDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG),
        )
    }
}
