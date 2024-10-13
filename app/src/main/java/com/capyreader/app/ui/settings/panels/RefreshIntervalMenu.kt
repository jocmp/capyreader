package com.capyreader.app.ui.settings.panels

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.refresher.RefreshInterval.EVERY_12_HOURS
import com.capyreader.app.refresher.RefreshInterval.EVERY_DAY
import com.capyreader.app.refresher.RefreshInterval.EVERY_FIFTEEN_MINUTES
import com.capyreader.app.refresher.RefreshInterval.EVERY_HOUR
import com.capyreader.app.refresher.RefreshInterval.EVERY_THIRTY_MINUTES
import com.capyreader.app.refresher.RefreshInterval.MANUALLY_ONLY
import com.capyreader.app.refresher.RefreshInterval.ON_START
import com.capyreader.app.ui.settings.PreferenceSelect

@Composable
fun RefreshIntervalMenu(
    refreshInterval: RefreshInterval,
    updateRefreshInterval: (interval: RefreshInterval) -> Unit,
) {
    val context = LocalContext.current

    PreferenceSelect(
        selected = refreshInterval,
        update = updateRefreshInterval,
        options = RefreshInterval.entries,
        optionText = { context.translationKey(it) },
        label = R.string.refresh_feeds_menu_label,
        disabledOption = MANUALLY_ONLY
    )
}

private fun Context.translationKey(refreshInterval: RefreshInterval): String {
    return when (refreshInterval) {
        MANUALLY_ONLY -> getString(R.string.refresh_manually_only)
        ON_START -> getString(R.string.refresh_on_start)
        EVERY_FIFTEEN_MINUTES -> getString(R.string.refresh_minutes, 15)
        EVERY_THIRTY_MINUTES -> getString(R.string.refresh_minutes, 30)
        EVERY_HOUR -> resources.getQuantityString(R.plurals.refresh_hours, 1, 1)
        EVERY_12_HOURS -> resources.getQuantityString(R.plurals.refresh_hours, 12, 12)
        EVERY_DAY -> getString(R.string.refresh_every_day)
    }
}

@Preview
@Composable
fun RefreshIntervalPreview() {
    RefreshIntervalMenu(
        refreshInterval = MANUALLY_ONLY,
        updateRefreshInterval = {}
    )
}
