package com.capyreader.app.ui.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capyreader.app.BuildConfig
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.opml.ImportProgress
import com.capyreader.app.R
import com.capyreader.app.common.ThemeOption
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.isCompact
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    refreshInterval: RefreshInterval,
    updateRefreshInterval: (interval: RefreshInterval) -> Unit,
    theme: ThemeOption,
    updateTheme: (theme: ThemeOption) -> Unit,
    onNavigateBack: () -> Unit,
    onRequestRemoveAccount: () -> Unit,
    onRequestExport: () -> Unit,
    onRequestImport: () -> Unit,
    canOpenLinksInternally: Boolean,
    updateOpenLinksInternally: (openLinksInternally: Boolean) -> Unit,
    accountSource: Source,
    accountName: String,
    importProgress: ImportProgress?,
) {
    val strings = AccountSettingsStrings.build(accountSource)
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

                Section(
                    title = stringResource(R.string.settings_section_display_appearance)
                ) {
                    ThemeMenu(onUpdateTheme = updateTheme, theme = theme)

                    TextSwitch(
                        checked = canOpenLinksInternally,
                        onCheckedChange = updateOpenLinksInternally,
                        text = {
                            Text(text = stringResource(R.string.settings_option_in_app_browser))
                        }
                    )
                }

                if (showImportButton(accountSource)) {
                    Section(title = stringResource(R.string.settings_section_import)) {
                        OPMLImportButton(
                            onClick = {
                                onRequestImport()
                            },
                            importProgress = importProgress
                        )
                    }
                }

                Section(title = stringResource(R.string.settings_section_export)) {
                    OPMLExportButton(
                        onClick = onRequestExport,
                    )
                }

                if (showCrashReporting()) {
                    Section(title = stringResource(R.string.settings_section_privacy)) {
                        CrashReportingCheckbox()
                    }
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = colorScheme.surfaceTint,
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

fun showCrashReporting() = BuildConfig.FLAVOR == "gplay"

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
            onNavigateBack = {},
            onRequestRemoveAccount = {},
            onRequestExport = {},
            onRequestImport = {},
            accountSource = Source.FEEDBIN,
            accountName = "hello@example.com",
            importProgress = null,
            updateTheme = {},
            theme = ThemeOption.LIGHT,
            canOpenLinksInternally = true,
            updateOpenLinksInternally = {},
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
        CapyTheme(theme = ThemeOption.DARK) {
            SettingsView(
                refreshInterval = RefreshInterval.EVERY_HOUR,
                updateRefreshInterval = {},
                onNavigateBack = {},
                onRequestRemoveAccount = {},
                onRequestExport = {},
                onRequestImport = {},
                accountSource = Source.LOCAL,
                accountName = "",
                importProgress = null,
                updateTheme = {},
                theme = ThemeOption.LIGHT,
                canOpenLinksInternally = true,
                updateOpenLinksInternally = {},
            )
        }
    }
}
