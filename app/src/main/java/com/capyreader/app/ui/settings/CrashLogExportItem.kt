package com.capyreader.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import com.capyreader.app.logging.CrashLogExport
import com.capyreader.app.ui.theme.CapyTheme
import kotlinx.coroutines.launch

@Composable
fun CrashLogExportItem(
    exception: Exception? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val export = {
        coroutineScope.launch {
            CrashLogExport(context).export(exception)
        }
    }

    Box(Modifier.clickable { export() }) {
        ListItem(
            headlineContent = {
                Text(stringResource(R.string.crash_log_export_item_title))
            },
            supportingContent = {
                Text(stringResource(R.string.crash_log_export_item_subtitle))
            }
        )
    }
}

@Preview
@Composable
private fun CrashScreenPreview() {
    CapyTheme() {
        CrashLogExportItem(exception = RuntimeException("Dummy"))
    }
}
