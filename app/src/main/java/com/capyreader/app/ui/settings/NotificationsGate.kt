package com.capyreader.app.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.capyreader.app.R
import com.capyreader.app.notifications.Notifications
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.components.LocalSnackbarHost
import com.jocmp.capy.common.launchUI
import org.koin.compose.koinInject

class NotificationsGate(
    val enabled: Boolean,
    val open: () -> Unit,
)

@Composable
fun rememberNotificationsGate(
    onNavigate: () -> Unit,
    appPreferences: AppPreferences = koinInject(),
): NotificationsGate {
    val refreshInterval by appPreferences.refreshInterval.collectChangesWithCurrent()
    val snackbar = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val enabled = refreshInterval.isPeriodic

    val permissions = rememberLauncherForActivityResult(RequestPermission()) { allowed ->
        if (allowed) {
            onNavigate()
        } else {
            scope.launchUI {
                val result = snackbar.showSnackbar(
                    message = context.getString(R.string.notifications_permission_disabled_title),
                    actionLabel = context.getString(R.string.notifications_permissions_disabled_call_to_action),
                    duration = SnackbarDuration.Short
                )

                if (result == SnackbarResult.ActionPerformed) {
                    context.openAppSettings()
                }
            }
        }
    }

    return NotificationsGate(enabled = enabled) {
        if (!enabled) {
            scope.launchUI {
                snackbar.showSnackbar(context.getString(R.string.settings_enable_refresh_call_to_action))
            }
        } else if (Notifications.askForPermission) {
            permissions.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onNavigate()
        }
    }
}

private fun Context.openAppSettings() {
    startActivity(Intent().apply {
        action = ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
    })
}
