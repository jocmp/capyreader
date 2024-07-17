package com.capyreader.app.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.refresher.RefreshInterval.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshIntervalMenu(
    refreshInterval: RefreshInterval,
    updateRefreshInterval: (interval: RefreshInterval) -> Unit,
) {
    val context = LocalContext.current
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val options = RefreshInterval.entries.map {
        it to context.translationKey(it)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { setExpanded(it) },
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = context.translationKey(refreshInterval),
            onValueChange = {},
            label = { Text(stringResource(R.string.refresh_feeds_menu_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) }
        ) {
            options.forEach { (interval, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        updateRefreshInterval(interval)
                        setExpanded(false)
                    }
                )
                if (interval == MANUALLY_ONLY) {
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun Context.translationKey(refreshInterval: RefreshInterval): String {
    return when (refreshInterval) {
        MANUALLY_ONLY -> getString(R.string.refresh_manually_only)
        EVERY_FIFTEEN_MINUTES -> getString(R.string.refresh_minutes, 15)
        EVERY_THIRTY_MINUTES -> getString(R.string.refresh_minutes, 30)
        EVERY_HOUR -> resources.getQuantityString(R.plurals.refresh_hours, 1)
        ON_START -> getString(R.string.refresh_on_start)
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
