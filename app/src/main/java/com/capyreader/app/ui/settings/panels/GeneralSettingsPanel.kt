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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.Notifications
import com.capyreader.app.R
import com.capyreader.app.common.RowItem
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.CrashReporting
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.settings.CrashReportingCheckbox
import com.capyreader.app.ui.settings.LocalSnackbarHost
import com.jocmp.capy.accounts.AutoDelete
import com.jocmp.capy.articles.UnreadSortOrder
import com.jocmp.capy.common.launchUI
import org.koin.androidx.compose.koinViewModel

@Composable
fun GeneralSettingsPanel(
    viewModel: GeneralSettingsViewModel = koinViewModel(),
    onNavigateToNotifications: () -> Unit,
) {
    GeneralSettingsPanelView(
        onNavigateToNotifications = onNavigateToNotifications,
        refreshInterval = viewModel.refreshInterval,
        updateRefreshInterval = viewModel::updateRefreshInterval,
        canOpenLinksInternally = viewModel.canOpenLinksInternally,
        updateOpenLinksInternally = viewModel::updateOpenLinksInternally,
        updateAutoDelete = viewModel::updateAutoDelete,
        autoDelete = viewModel.autoDelete,
        onClearArticles = viewModel::clearAllArticles,
        updateUnreadSort = viewModel::updateUnreadSort,
        unreadSort = viewModel.unreadSort,
    )
}

@Composable
fun GeneralSettingsPanelView(
    onNavigateToNotifications: () -> Unit,
    onClearArticles: () -> Unit,
    refreshInterval: RefreshInterval,
    updateRefreshInterval: (RefreshInterval) -> Unit,
    canOpenLinksInternally: Boolean,
    updateOpenLinksInternally: (canOpenLinksInternally: Boolean) -> Unit,
    updateAutoDelete: (AutoDelete) -> Unit,
    autoDelete: AutoDelete,
    updateUnreadSort: (UnreadSortOrder) -> Unit,
    unreadSort: UnreadSortOrder,
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
        UnreadSortOrderSelect(
            unreadSort,
            updateUnreadSort
        )

        FormSection(title = stringResource(R.string.settings_section_refresh)) {
            RefreshIntervalMenu(
                refreshInterval = refreshInterval,
                updateRefreshInterval = updateRefreshInterval,
            )
            NotificationsListItem(
                onNavigate = onNavigateToNotifications,
                refreshInterval = refreshInterval,
            )
        }

        if (CrashReporting.isAvailable) {
            FormSection(title = stringResource(R.string.settings_section_privacy)) {
                RowItem {
                    CrashReportingCheckbox()
                }
            }
        }

        RowItem {
            TextSwitch(
                checked = canOpenLinksInternally,
                onCheckedChange = updateOpenLinksInternally,
                title = stringResource(R.string.settings_option_in_app_browser)
            )
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
                Button(
                    onClick = { setClearArticlesDialogOpen(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondary,
                        contentColor = colorScheme.onSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_clear_articles_button))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }

    if (isClearArticlesDialogOpen) {
        AlertDialog(
            onDismissRequest = onClearArticlesCancel,
            text = { Text(stringResource(R.string.settings_clear_articles_text)) },
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
    GeneralSettingsPanelView(
        refreshInterval = RefreshInterval.EVERY_HOUR,
        updateRefreshInterval = {},
        canOpenLinksInternally = false,
        onClearArticles = {},
        updateOpenLinksInternally = {},
        updateAutoDelete = {},
        autoDelete = AutoDelete.WEEKLY,
        unreadSort = UnreadSortOrder.NEWEST_FIRST,
        updateUnreadSort = {},
        onNavigateToNotifications = {}
    )
}
