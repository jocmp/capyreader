package com.capyreader.app.ui.settings

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.BuildConfig.VERSION_NAME
import com.capyreader.app.R
import com.capyreader.app.common.openLink
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun AboutSettingsPanel() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val copyVersionToClipboard = {
        clipboardManager.setText(AnnotatedString("Capy Reader $VERSION_NAME"))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column {
            Box(
                modifier = Modifier.clickable {
                    context.openLink(Uri.parse(Support.URL))
                }
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_support_button)) }
                )
            }
        }

        FormSection(title = stringResource(R.string.settings_section_version)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        copyVersionToClipboard()
                    }
            ) {
                Text(
                    text = VERSION_NAME,
                    modifier = Modifier
                        .padding(16.dp)
                )
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    tint = colorScheme.secondary,
                    contentDescription = stringResource(
                        R.string.settings_option_copy_version
                    ),
                    modifier = Modifier
                        .padding(end = 16.dp)
                )
            }
        }
        HorizontalDivider()
        FormSection {
            Box(Modifier.padding(horizontal = 4.dp)) {
                TextButton(onClick = { context.openLink(Uri.parse(Support.ABOUT_URL)) }) {
                    Text("Made with ♥ in ✶✶✶✶")
                }
            }
        }
    }
}

private object Support {
    const val URL = "https://capyreader.com/support"

    const val ABOUT_URL = "https://jocmp.com"
}

@Preview
@Composable
private fun AboutSettingsPanelPreview() {
    CapyTheme {
        AboutSettingsPanel()
    }
}
