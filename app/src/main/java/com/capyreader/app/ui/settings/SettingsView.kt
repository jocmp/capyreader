package com.capyreader.app.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.capyreader.app.BuildConfig
import com.capyreader.app.BuildConfig.VERSION_NAME
import com.capyreader.app.R
import com.capyreader.app.common.ThemeOption
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.isCompact
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.accounts.AutoDelete
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.opml.ImportProgress
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@Immutable
data class SettingsOptions(
    val canOpenLinksInternally: Boolean,
    val refreshInterval: RefreshInterval,
    val autoDelete: AutoDelete,
    val updateAutoDelete: (option: AutoDelete) -> Unit,
    val theme: ThemeOption,
    val updateOpenLinksInternally: (openLinksInternally: Boolean) -> Unit,
    val updateRefreshInterval: (interval: RefreshInterval) -> Unit,
    val updateStickFullContent: (enable: Boolean) -> Unit,
    val updateTheme: (theme: ThemeOption) -> Unit,
    val articleList: ArticleListOptions,
    val enableStickyFullContent: Boolean,
)

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsView(
    onNavigateBack: () -> Unit,
    onRemoveAccount: () -> Unit,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator(
        initialDestinationHistory = initialDestinationHistory()
    )
    val currentPanel = navigator.currentDestination?.content

    SettingsScaffold(
        scaffoldNavigator = navigator,
        listPane = {
            SettingsList(
                selected = currentPanel,
                onNavigate = { panel ->
                    navigator.navigateTo(ThreePaneScaffoldRole.Primary, panel)
                },
                onNavigateBack = onNavigateBack
            )
        },
        detailPane = {
            currentPanel?.let { panel ->
                SettingsPanelScaffold(
                    onBack = {
                        navigator.navigateBack()
                    },
                    title = panel.title,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        when (panel) {
                            is SettingsPanel.General -> GeneralSettingsPanel(
                                onRemoveAccount = onRemoveAccount
                            )
                            SettingsPanel.About -> AboutSettingsPanel()
                            SettingsPanel.Display -> Text("Sorry charlie")
                            SettingsPanel.ImportExport -> Text("Sorry charlie")
                        }
                    }
                }
            }
        }
    )

    BackHandler(currentPanel == SettingsPanel.General) {
        onNavigateBack()
    }

    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OldSettingsView(
    onNavigateBack: () -> Unit,
    onRequestRemoveAccount: () -> Unit,
    onRequestExport: () -> Unit,
    onRequestImport: () -> Unit,
    accountSource: Source,
    accountName: String,
    importProgress: ImportProgress?,
    settings: SettingsOptions,
) {
    val scrollBehavior = enterAlwaysScrollBehavior()
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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                scrollBehavior = scrollBehavior,
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
                Spacer(modifier = Modifier.padding(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    FormSection(title = stringResource(R.string.settings_section_refresh)) {
                        RowItem {
                            RefreshIntervalMenu(
                                refreshInterval = settings.refreshInterval,
                                updateRefreshInterval = settings.updateRefreshInterval,
                            )
                        }

                        RowItem {
                            TextSwitch(
                                onCheckedChange = { enabled ->
                                    settings.updateAutoDelete(
                                        if (enabled) AutoDelete.EVERY_THREE_MONTHS else AutoDelete.DISABLED
                                    )
                                },
                                checked = settings.autoDelete == AutoDelete.EVERY_THREE_MONTHS,
                                title = stringResource(R.string.settings_option_auto_delete_articles_title),
                                subtitle = stringResource(R.string.settings_option_auto_delete_articles_subtitle)
                            )
                        }
                    }

                    FormSection(
                        title = stringResource(R.string.settings_section_display_appearance)
                    ) {
                        RowItem {
                            ThemeMenu(onUpdateTheme = settings.updateTheme, theme = settings.theme)
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
                        FormSection(title = stringResource(R.string.settings_section_import)) {
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

                    FormSection(title = stringResource(R.string.settings_section_export)) {
                        RowItem {
                            OPMLExportButton(
                                onClick = onRequestExport,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
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
private fun backButton(): ImageVector {
    val showBackArrow = isCompact()

    return if (showBackArrow) {
        Icons.AutoMirrored.Filled.ArrowBack
    } else {
        Icons.Rounded.Close
    }
}

fun showImportButton(source: Source): Boolean {
    return source == Source.LOCAL
}

fun showCrashReporting() = BuildConfig.FLAVOR == "gplay"

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun initialDestinationHistory(): List<ThreePaneScaffoldDestinationItem<SettingsPanel>> {
    return if (isCompact()) {
        listOf(
            ThreePaneScaffoldDestinationItem<SettingsPanel>(
                ListDetailPaneScaffoldRole.List,
                null
            )
        )
    } else {
        listOf(
            ThreePaneScaffoldDestinationItem(
                pane = ThreePaneScaffoldRole.Secondary,
                SettingsPanel.General
            )
        )
    }
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
            onNavigateBack = {},
            onRemoveAccount = {}
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
                onRemoveAccount = {}
            )
        }
    }
}
