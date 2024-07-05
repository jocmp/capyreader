package com.jocmp.capyreader.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.opml.ImportProgress
import com.jocmp.capyreader.R
import com.jocmp.capyreader.refresher.RefreshInterval
import com.jocmp.capyreader.setupCommonModules
import com.jocmp.capyreader.ui.components.CrashReportingCheckbox
import com.jocmp.capyreader.ui.isCompact
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    refreshInterval: RefreshInterval,
    updateRefreshInterval: (interval: RefreshInterval) -> Unit,
    onNavigateBack: () -> Unit,
    onRequestRemoveAccount: () -> Unit,
    onRequestExport: () -> Unit,
    onRequestImport: () -> Unit,
    accountSource: Source,
    accountName: String,
    importProgressPercent: Int?
) {
    val context = LocalContext.current
    val strings = AccountSettingsStrings.find(accountSource)
    val (isRemoveDialogOpen, setRemoveDialogOpen) = remember { mutableStateOf(false) }

    val onRemoveCancel = {
        setRemoveDialogOpen(false)
    }

    val onRemove = {
        setRemoveDialogOpen(false)
        onRequestRemoveAccount()
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(text = stringResource(R.string.settings_top_bar_title))
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onNavigateBack() }
                    ) {
                        Icon(
                            imageVector = backButton(),
                            contentDescription = null
                        )
                    }
                },
            )
        },
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxHeight()
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                if (showAccountName(accountSource)) {
                    Section(
                        title = stringResource(R.string.settings_section_account),
                    ) {
                        Text(text = accountName)
                    }
                }

                Section(title = stringResource(R.string.settings_section_refresh)) {
                    RefreshIntervalMenu(
                        refreshInterval = refreshInterval,
                        updateRefreshInterval = updateRefreshInterval,
                    )
                }

                if (showImportButton(accountSource)) {
                    Section(title = stringResource(R.string.settings_section_import)) {
                        importProgressPercent?.let { progress ->
                            Text("Importing subscriptions... ${progress}%")
                        }
                        OPMLImportButton(onClick = onRequestImport)
                    }
                }

                Section(title = stringResource(R.string.settings_section_export)) {
                    OPMLExportButton(
                        onClick = onRequestExport,
                    )
                }

                Section(title = stringResource(R.string.settings_section_privacy)) {
                    CrashReportingCheckbox()
                }

                Section {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                    Button(
                        onClick = { setRemoveDialogOpen(true) },
                        colors = removeAccountButtonColors(accountSource),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(strings.requestRemoveText))
                    }
                }
            }
        }
    }

    if (isRemoveDialogOpen) {
        AlertDialog(
            onDismissRequest = onRemoveCancel,
            title = { Text(stringResource(strings.dialogTitle)) },
            text = { Text(stringResource(strings.dialogMessage)) },
            dismissButton = {
                TextButton(onClick = onRemoveCancel) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = onRemove) {
                    Text(text = stringResource(strings.dialogConfirmText))
                }
            }
        )
    }
}

@Composable
fun Section(
    title: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        Modifier
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = colorScheme.surfaceTint,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        content()
    }
}

@Composable
private fun backButton(): ImageVector {
    val showBackArrow = isCompact()

    return if (showBackArrow) {
        Icons.AutoMirrored.Filled.ArrowBack
    } else {
        Icons.Filled.Close
    }
}

@Composable
fun removeAccountButtonColors(source: Source) = when (source) {
    Source.LOCAL -> ButtonDefaults.buttonColors(
        containerColor = colorScheme.error,
        contentColor = colorScheme.onError
    )

    Source.FEEDBIN -> ButtonDefaults.buttonColors(
        containerColor = colorScheme.secondary,
        contentColor = colorScheme.onSecondary
    )
}

fun showImportButton(source: Source): Boolean {
    return source == Source.LOCAL
}

fun showAccountName(source: Source): Boolean {
    return source == Source.FEEDBIN
}

@Preview
@Composable
fun AccountSettingsViewPreview() {
    val context = LocalContext.current

    KoinApplication(
        application = {
            androidContext(context)
            setupCommonModules()
        }
    ) {
        SettingsView(
            refreshInterval = RefreshInterval.EVERY_HOUR,
            updateRefreshInterval = {},
            onRequestRemoveAccount = {},
            onNavigateBack = {},
            onRequestExport = {},
            onRequestImport = {},
            accountSource = Source.FEEDBIN,
            accountName = "hello@example.com",
            importProgressPercent = null,
        )
    }
}

@Preview
@Composable
fun AccountSettingsView_LocalPreview() {
    val context = LocalContext.current

    KoinApplication(
        application = {
            androidContext(context)
            setupCommonModules()
        }
    ) {
        SettingsView(
            refreshInterval = RefreshInterval.EVERY_HOUR,
            updateRefreshInterval = {},
            onRequestRemoveAccount = {},
            onNavigateBack = {},
            onRequestExport = {},
            onRequestImport = {},
            accountSource = Source.LOCAL,
            accountName = "",
            importProgressPercent = null,
        )
    }
}
