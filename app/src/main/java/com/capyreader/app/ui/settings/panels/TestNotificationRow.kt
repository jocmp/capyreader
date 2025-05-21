package com.capyreader.app.ui.settings.panels

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.capyreader.app.common.RowItem
import com.capyreader.app.notifications.NotificationHelper
import com.jocmp.capy.Account
import com.jocmp.capy.common.launchIO
import org.koin.compose.koinInject
import java.time.ZonedDateTime

@Composable
fun TestNotificationRow(account: Account = koinInject()) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun notify() {
        scope.launchIO {
            account.refresh()
            NotificationHelper(account = account, context).notify(
                since = ZonedDateTime.now().minusHours(24)
            )
        }
    }

    RowItem {
        Button(onClick = { notify() }) {
            Text("Test Notifications")
        }
    }
}
