package com.jocmp.capyreader.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.jocmp.capy.Account
import com.jocmp.capyreader.R
import com.jocmp.capyreader.transfers.OPMLExporter
import org.koin.compose.koinInject

@Composable
fun OPMLExportButton(
    onClick: () -> Unit,
) {
    Column {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.secondary,
                contentColor = colorScheme.onSecondary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.opml_export_button_text))
        }
    }
}
