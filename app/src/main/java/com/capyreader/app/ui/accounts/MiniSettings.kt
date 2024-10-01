package com.capyreader.app.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.capyreader.app.R
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.CrashReporting
import com.capyreader.app.ui.components.DialogCard
import com.capyreader.app.ui.settings.CrashLogExportItem
import com.capyreader.app.ui.settings.CrashReportingCheckbox
import com.capyreader.app.ui.settings.RowItem
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniSettings() {
    val (isOpen, setOpen) = remember { mutableStateOf(false) }

    val onNavigateBack = {
        setOpen(false)
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = { setOpen(true) }) {
            Icon(
                Icons.Rounded.Settings,
                contentDescription = stringResource(R.string.settings)
            )
        }
    }

    if (isOpen) {
        Dialog(onDismissRequest = onNavigateBack) {
            DialogCard {
                TopAppBar(
                    colors = TopAppBarDefaults.mediumTopAppBarColors().copy(
                        containerColor = colorScheme.surfaceVariant
                    ),
                    title = {
                        Text(stringResource(R.string.settings_top_bar_title))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(android.R.string.cancel)
                            )
                        }
                    }
                )
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .background(colorScheme.surfaceVariant)
                        .padding(bottom = 16.dp)
                ) {
                    RowItem {
                        if (CrashReporting.isAvailable) {
                            CrashReportingCheckbox()
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun MiniSettingsPreview() {
    val context = LocalContext.current

    KoinApplication(
        application = {
            androidContext(context)
            setupCommonModules()
        }
    ) {
        CapyTheme {
            MiniSettings()
        }
    }
}
