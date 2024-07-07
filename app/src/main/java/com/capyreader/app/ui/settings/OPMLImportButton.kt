package com.capyreader.app.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
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
import com.jocmp.capy.opml.ImportProgress
import com.capyreader.app.R
import com.capyreader.app.ui.theme.CapyTheme

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
            stringResource(
                R.string.settings_import_progress,
                progress.currentCount,
                progress.total
            ),
            fontStyle = FontStyle.Italic
        )
    } else {
        Text(stringResource(R.string.opml_import_button_text))
    }
}


@Preview
@Composable
private fun OPMLImportButtonPreview() {
    CapyTheme {
        OPMLImportButton(
            onClick = {},
            importProgress = ImportProgress(currentCount = 0, total = 10)
        )
    }
}
