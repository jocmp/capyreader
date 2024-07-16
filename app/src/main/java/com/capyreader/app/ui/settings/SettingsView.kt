package com.capyreader.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.capyreader.app.BuildConfig
import com.capyreader.app.BuildConfig.VERSION_NAME
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.common.ThemeOption
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.isCompact
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.opml.ImportProgress
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@Immutable
data class SettingsOptions(
    val canOpenLinksInternally: Boolean,
    val refreshInterval: RefreshInterval,
    val theme: ThemeOption,
    val updateOpenLinksInternally: (openLinksInternally: Boolean) -> Unit,
    val updateRefreshInterval: (interval: RefreshInterval) -> Unit,
    val updateStickFullContent: (enable: Boolean) -> Unit,
    val updateTheme: (theme: ThemeOption) -> Unit,
    val articleList: ArticleListOptions,
    val enableStickyFullContent: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    onNavigateBack: () -> Unit,
    onRequestRemoveAccount: () -> Unit,
    onRequestExport: () -> Unit,
    onRequestImport: () -> Unit,
    accountSource: Source,
    accountName: String,
    importProgress: ImportProgress?,
    settings: SettingsOptions,
) {
    val clipboardManager = LocalClipboardManager.current
    val strings = AccountSettingsStrings.build(accountSource)
    val (isRemoveDialogOpen, setRemoveDialogOpen) = remember { mutableStateOf(false) }
    val (isAdvancedDisplayDialogOpen, setAdvancedDisplayDialogOpen) = remember {
        mutableStateOf(false)
    }

    val copyVersionToClipboard = {
        clipboardManager.setText(AnnotatedString("Capy Reader $VERSION_NAME"))
    }

    val onRemoveCancel = {
        setRemoveDialogOpen(false)
    }

    val onRemove = {
        setRemoveDialogOpen(false)
        onRequestRemoveAccount()
    }

    val showAdvancedDisplaySettings = {
        setAdvancedDisplayDialogOpen(true)
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
                        RowItem {
                            Text(text = accountName)
                        }
                    }
                }

                Section(title = stringResource(R.string.settings_section_refresh)) {
                    RowItem {
                        RefreshIntervalMenu(
                            refreshInterval = settings.refreshInterval,
                            updateRefreshInterval = settings.updateRefreshInterval,
                        )
                    }
                }

                Section(
                    title = stringResource(R.string.settings_section_display_appearance)
                ) {
                    RowItem {
                        ThemeMenu(onUpdateTheme = settings.updateTheme, theme = settings.theme)
                    }

                    RowItem {
                        TextSwitch(
                            checked = settings.canOpenLinksInternally,
                            onCheckedChange = settings.updateOpenLinksInternally,
                            title = stringResource(R.string.settings_option_in_app_browser)
                        )
                    }

                    RowItem {
                        TextSwitch(
                            checked = settings.enableStickyFullContent,
                            onCheckedChange = settings.updateStickFullContent,
                            title = stringResource(R.string.settings_option_full_content_title),
                            subtitle = stringResource(R.string.settings_option_full_content_subtitle)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAdvancedDisplaySettings()
                            }
                    ) {
                        Text(
                            text = stringResource(R.string.settings_more_display_options_button),
                            modifier = Modifier.padding(16.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }

                if (showImportButton(accountSource)) {
                    Section(title = stringResource(R.string.settings_section_import)) {
                        RowItem {
                            OPMLImportButton(
                                onClick = {
                                    onRequestImport()
                                },
                                importProgress = importProgress
                            )
                        }
                    }
                }

                Section(title = stringResource(R.string.settings_section_export)) {
                    RowItem {
                        OPMLExportButton(
                            onClick = onRequestExport,
                        )
                    }
                }

                if (showCrashReporting()) {
                    Section(title = stringResource(R.string.settings_section_privacy)) {
                        RowItem {
                            CrashReportingCheckbox()
                        }
                    }
                }

                Section(stringResource(R.string.settings_section_version)) {
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

                Section {
                    RowItem {
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

    if (isAdvancedDisplayDialogOpen) {
        Dialog(
            onDismissRequest = { setAdvancedDisplayDialogOpen(false) },
            properties = DialogProperties(usePlatformDefaultWidth = isCompact())
        ) {
            ArticleListSettings(
                onRequestClose = { setAdvancedDisplayDialogOpen(false) },
                options = settings.articleList
            )
        }
    }
}

@Composable
fun Section(
    title: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = colorScheme.surfaceTint,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        content()
    }
}

@Composable
private fun RowItem(
    skipPadding: Boolean = false,
    content: @Composable () -> Unit
) {
    if (skipPadding) {
        content()
    } else {
        Column(Modifier.padding(horizontal = 16.dp)) {
            content()
        }
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
            onNavigateBack = {},
            onRequestRemoveAccount = {},
            onRequestExport = {},
            onRequestImport = {},
            accountSource = Source.FEEDBIN,
            accountName = "hello@example.com",
            importProgress = null,
            settings = SettingsOptions(
                refreshInterval = RefreshInterval.EVERY_HOUR,
                updateRefreshInterval = {},
                updateTheme = {},
                theme = ThemeOption.LIGHT,
                canOpenLinksInternally = true,
                updateOpenLinksInternally = {},
                enableStickyFullContent = false,
                updateStickFullContent = {},
                articleList = ArticleListOptions(
                    imagePreview = ImagePreview.default,
                    showSummary = false,
                    showFeedName = false,
                    showFeedIcons = false,
                    updateSummary = {},
                    updateFeedIcons = {},
                    updateImagePreview = {},
                    updateFeedName = {}
                )
            )
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
                onNavigateBack = {},
                onRequestRemoveAccount = {},
                onRequestExport = {},
                onRequestImport = {},
                accountSource = Source.LOCAL,
                accountName = "",
                importProgress = null,
                settings = SettingsOptions(
                    refreshInterval = RefreshInterval.EVERY_HOUR,
                    updateRefreshInterval = {},
                    updateTheme = {},
                    theme = ThemeOption.LIGHT,
                    canOpenLinksInternally = true,
                    updateOpenLinksInternally = {},
                    enableStickyFullContent = false,
                    updateStickFullContent = {},
                    articleList = ArticleListOptions(
                        imagePreview = ImagePreview.default,
                        showSummary = false,
                        showFeedName = false,
                        showFeedIcons = false,
                        updateSummary = {},
                        updateFeedIcons = {},
                        updateImagePreview = {},
                        updateFeedName = {},
                    )
                )
            )
        }
    }
}
