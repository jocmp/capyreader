package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R

@Composable
fun OPMLExportButton(
    onClick: () -> Unit,
) {
    Column {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.opml_export_button_text))
        }
    }
}
