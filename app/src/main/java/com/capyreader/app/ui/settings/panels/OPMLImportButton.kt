package com.capyreader.app.ui.settings.panels

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.opml.ImportProgress

@Composable
fun OPMLImportButton(
    onClick: () -> Unit,
    importProgress: ImportProgress? = null
) {
    val permissions = rememberLauncherForActivityResult(RequestPermission()) { _ ->
        onClick()
    }

    Button(
        enabled = importProgress == null,
        onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onClick()
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.secondary,
            contentColor = colorScheme.onSecondary
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        ButtonText(importProgress)
    }
}

@Composable
private fun ButtonText(progress: ImportProgress?) {
    if (progress != null) {
        Text(
            text(progress),
            fontStyle = FontStyle.Italic
        )
    } else {
        Text(stringResource(R.string.opml_import_button_text))
    }
}

@Composable
fun text(progress: ImportProgress): String {
    return if (progress.total == 0) {
        stringResource(
            R.string.settings_import_progress_placeholder
        )
    } else {
        stringResource(
            R.string.settings_import_progress,
            progress.currentCount,
            progress.total
        )
    }
}

@Preview
@Composable
private fun OPMLImportButtonPreview() {
    CapyTheme {
        Column {
            OPMLImportButton(
                onClick = {},
                importProgress = ImportProgress(currentCount = 0, total = 10)
            )
            OPMLImportButton(
                onClick = {},
                importProgress = ImportProgress(currentCount = 0, total = 0)
            )
        }
    }
}
