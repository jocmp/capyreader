<<<<<<<< HEAD:app/src/main/java/com/capyreader/ui/components/CrashReportingCheckbox.kt
package com.capyreader.ui.components
========
package com.jocmp.capyreader.ui.settings
>>>>>>>> 07507dc (wip):app/src/main/java/com/capyreader/ui/settings/CrashReportingCheckbox.kt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.capyreader.R
import com.capyreader.common.AppPreferences
import com.capyreader.setupCommonModules
import com.capyreader.ui.theme.CapyTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun CrashReportingCheckbox(
    appPreferences: AppPreferences = koinInject()
) {
    val (enableCrashReporting, setCrashReportingEnabled) = rememberSaveable {
        mutableStateOf(appPreferences.crashReporting.get())
    }

    val updateCrashReporting = { enabled: Boolean ->
        setCrashReportingEnabled(enabled)
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(enabled)
        appPreferences.crashReporting.set(enabled)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.crash_reporting_checkbox_title))
        Switch(checked = enableCrashReporting, onCheckedChange = updateCrashReporting)
    }
}

@Preview
@Composable
private fun CrashReportingCheckboxPreview() {
    val context = LocalContext.current

    KoinApplication(
        application = {
            androidContext(context)
            setupCommonModules()
        }
    ) {
        CapyTheme {
            CrashReportingCheckbox()
        }
    }
}
