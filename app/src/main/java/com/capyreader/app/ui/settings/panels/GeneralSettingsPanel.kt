package com.capyreader.app.ui.settings.panels

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capyreader.app.BuildConfig
import com.capyreader.app.R
import com.capyreader.app.common.RowItem
import com.capyreader.app.notifications.Notifications
import com.capyreader.app.preferences.AfterReadAllBehavior
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.CrashReporting
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.fixtures.PreviewKoinApplication
import com.capyreader.app.ui.settings.CrashReportingCheckbox
import com.capyreader.app.ui.settings.LocalSnackbarHost
import com.capyreader.app.ui.settings.PreferenceSelect
import com.capyreader.app.ui.settings.keywordblocklist.BlockedKeywords
import com.capyreader.app.ui.settings.keywordblocklist.KeywordBlocklistItem
import com.capyreader.app.ui.settings.keywordblocklist.LocalBlockedKeywords
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.accounts.AutoDelete
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.common.launchUI
import org.koin.androidx.compose.koinViewModel
import java.lang.String.CASE_INSENSITIVE_ORDER

@Composable
fun GeneralSettingsPanel(
    viewModel: GeneralSettingsViewModel = koinViewModel(),
    onNavigateToNotifications: () -> Unit,
) {
    val keywords by viewModel.keywordBlocklist.collectAsStateWithLifecycle()

    val blockedKeywords = BlockedKeywords(
        keywords = keywords.toList().sortedWith(compareBy(CASE_INSENSITIVE_ORDER) { it }),
        remove = viewModel::removeBlockedKeyword,
        add = viewModel::addBlockedKeyword,
    )

    CompositionLocalProvider(
        LocalBlockedKeywords provides blockedKeywords
    ) {
        GeneralSettingsPanelView(
            source = viewModel.source,
            onNavigateToNotifications = onNavigateToNotifications,
            refreshInterval = viewModel.refreshInterval,
            updateRefreshInterval = viewModel::updateRefreshInterval,
            canOpenLinksInternally = viewModel.canOpenLinksInternally,
            updateOpenLinksInternally = viewModel::updateOpenLinksInternally,
            updateAutoDelete = viewModel::updateAutoDelete,
            autoDelete = viewModel.autoDelete,
            onClearArticles = viewModel::clearAllArticles,
            updateSortOrder = viewModel::updateSortOrder,
            sortOrder = viewModel.sortOrder,
            updateConfirmMarkAllRead = viewModel::updateConfirmMarkAllRead,
            updateMarkReadOnScroll = viewModel::updateMarkReadOnScroll,
            confirmMarkAllRead = viewModel.confirmMarkAllRead,
            markReadOnScroll = viewModel.markReadOnScroll,
            afterReadAll = viewModel.afterReadAll,
            updateAfterReadAll = viewModel::updateAfterReadAll,
            updateStickyFullContent = viewModel::updateStickyFullContent,
            enableStickyFullContent = viewModel.enableStickyFullContent,
            showTodayFilter = viewModel.showTodayFilter,
            updateShowTodayFilter = viewModel::updateShowTodayFilter,
        )
    }
}

@Composable
fun GeneralSettingsPanelView(
    source: Source,
    onNavigateToNotifications: () -> Unit,
    onClearArticles: () -> Unit,
    refreshInterval: RefreshInterval,
    updateRefreshInterval: (RefreshInterval) -> Unit,
    canOpenLinksInternally: Boolean,
    updateOpenLinksInternally: (canOpenLinksInternally: Boolean) -> Unit,
    updateAutoDelete: (AutoDelete) -> Unit,
    autoDelete: AutoDelete,
    updateSortOrder: (SortOrder) -> Unit,
    sortOrder: SortOrder,
    updateStickyFullContent: (enable: Boolean) -> Unit,
    enableStickyFullContent: Boolean,
    updateConfirmMarkAllRead: (enable: Boolean) -> Unit,
    updateMarkReadOnScroll: (enable: Boolean) -> Unit,
    afterReadAll: AfterReadAllBehavior,
    updateAfterReadAll: (behavior: AfterReadAllBehavior) -> Unit,
    confirmMarkAllRead: Boolean,
    markReadOnScroll: Boolean,
    showTodayFilter: Boolean,
    updateShowTodayFilter: (show: Boolean) -> Unit,
) {
    val (isClearArticlesDialogOpen, setClearArticlesDialogOpen) = remember { mutableStateOf(false) }

    val onClearArticlesCancel = {
        setClearArticlesDialogOpen(false)
    }

    val onRequestClearArticles = {
        setClearArticlesDialogOpen(false)
        onClearArticles()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        SortOrderSelect(
            sortOrder,
            updateSortOrder
        )

        FormSection(title = stringResource(R.string.settings_section_categories)) {
            RowItem {
                TextSwitch(
                    checked = showTodayFilter,
                    onCheckedChange = updateShowTodayFilter,
                    title = stringResource(R.string.settings_option_show_today_filter)
                )
            }
        }

        FormSection(title = stringResource(R.string.settings_section_refresh)) {
            Column {
                RefreshIntervalMenu(
                    refreshInterval = refreshInterval,
                    updateRefreshInterval = updateRefreshInterval,
                )
                NotificationsListItem(
                    onNavigate = onNavigateToNotifications,
                    refreshInterval = refreshInterval,
                )
                if (source == Source.LOCAL) {
                    KeywordBlocklistItem()
                }
            }
        }

        if (CrashReporting.isAvailable) {
            FormSection(title = stringResource(R.string.settings_section_privacy)) {
                RowItem {
                    CrashReportingCheckbox()
                }
            }
        }

        FormSection(title = stringResource(R.string.settings_section_browser)) {
            RowItem {
                TextSwitch(
                    checked = canOpenLinksInternally,
                    onCheckedChange = updateOpenLinksInternally,
                    title = stringResource(R.string.settings_option_in_app_browser)
                )
            }
        }

        FormSection(title = stringResource(R.string.settings_reader_title)) {
            RowItem {
                TextSwitch(
                    checked = enableStickyFullContent,
                    onCheckedChange = updateStickyFullContent,
                    title = stringResource(R.string.settings_option_full_content_title),
                    subtitle = stringResource(R.string.settings_option_full_content_subtitle)
                )
            }
        }

        FormSection(
            title = stringResource(R.string.settings_section_mark_all_as_read),
        ) {
            Column {
                RowItem {
                    TextSwitch(
                        onCheckedChange = updateConfirmMarkAllRead,
                        checked = confirmMarkAllRead,
                        title = stringResource(R.string.settings_confirm_mark_all_read),
                    )
                    TextSwitch(
                        onCheckedChange = updateMarkReadOnScroll,
                        checked = markReadOnScroll,
                        title = stringResource(R.string.settings_mark_read_on_scroll),
                    )
                }
                PreferenceSelect(
                    selected = afterReadAll,
                    update = updateAfterReadAll,
                    options = AfterReadAllBehavior.entries,
                    label = R.string.after_read_all_behavior_label,
                    optionText = {
                        stringResource(id = it.translationKey)
                    }
                )
            }
        }

        FormSection(
            title = stringResource(R.string.settings_section_advanced)
        ) {
            Column {
                CrashLogExportItem()

                AutoDeleteMenu(
                    updateAutoDelete = updateAutoDelete,
                    autoDelete = autoDelete,
                )
            }

            RowItem {
                FilledTonalButton(
                    onClick = { setClearArticlesDialogOpen(true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_clear_all_articles_button))
                }
            }

            if (BuildConfig.DEBUG && !LocalView.current.isInEditMode) {
                TestNotificationRow()
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    if (isClearArticlesDialogOpen) {
        AlertDialog(
            onDismissRequest = onClearArticlesCancel,
            text = { Text(stringResource(R.string.settings_clear_all_articles_text)) },
            dismissButton = {
                TextButton(onClick = onClearArticlesCancel) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = onRequestClearArticles) {
                    Text(text = stringResource(R.string.settings_clear_all_articles_confirm))
                }
            }
        )
    }
}

@Composable
fun NotificationsListItem(
    onNavigate: () -> Unit,
    refreshInterval: RefreshInterval,
) {
    val defaultColors = ListItemDefaults.colors()
    val enabled = refreshInterval.isPeriodic
    val snackbar = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun showPermissionFailureMessage() {
        scope.launchUI {
            snackbar.showSnackbar(
                message = context.getString(R.string.notifications_permission_disabled_title),
                actionLabel = context.getString(R.string.notifications_permissions_disabled_call_to_action),
                duration = SnackbarDuration.Short
            ).let { result ->
                if (result == SnackbarResult.ActionPerformed) {
                    context.openAppSettings()
                }
            }
        }
    }

    val colors = ListItemDefaults.colors(
        headlineColor = if (enabled) defaultColors.headlineColor else defaultColors.disabledHeadlineColor,
        supportingColor = if (enabled) defaultColors.supportingTextColor else defaultColors.disabledHeadlineColor,
    )

    val permissions = rememberLauncherForActivityResult(RequestPermission()) { allowed ->
        if (allowed) {
            onNavigate()
        } else {
            showPermissionFailureMessage()
        }
    }

    Box(
        Modifier.clickable(
            enabled = enabled
        ) {
            if (Notifications.askForPermission) {
                permissions.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onNavigate()
            }
        }
    ) {
        ListItem(
            colors = colors,
            headlineContent = {
                Text(stringResource(R.string.settings_panel_notifications_title))
            },
            supportingContent = {
                if (!enabled) {
                    Text(stringResource(R.string.settings_enable_refresh_call_to_action))
                }
            }
        )
    }
}

private fun Context.openAppSettings() {
    startActivity(Intent().apply {
        action = ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
    })
}

@Preview
@Composable
private fun GeneralSettingsPanelPreview() {
    PreviewKoinApplication {
        CapyTheme {
            GeneralSettingsPanelView(
                source = Source.LOCAL,
                refreshInterval = RefreshInterval.EVERY_HOUR,
                updateRefreshInterval = {},
                canOpenLinksInternally = false,
                onClearArticles = {},
                updateOpenLinksInternally = {},
                updateAutoDelete = {},
                autoDelete = AutoDelete.WEEKLY,
                sortOrder = SortOrder.NEWEST_FIRST,
                updateSortOrder = {},
                onNavigateToNotifications = {},
                markReadOnScroll = true,
                updateConfirmMarkAllRead = {},
                updateMarkReadOnScroll = {},
                confirmMarkAllRead = true,
                updateStickyFullContent = {},
                enableStickyFullContent = true,
                afterReadAll = AfterReadAllBehavior.NOTHING,
                updateAfterReadAll = {},
                showTodayFilter = true,
                updateShowTodayFilter = {},
            )
        }
    }
}
