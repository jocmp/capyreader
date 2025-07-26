package com.capyreader.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.theme.CapyTheme
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
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
        Firebase.crashlytics.isCrashlyticsCollectionEnabled = enabled
        appPreferences.crashReporting.set(enabled)
    }

    TextSwitch(
        checked = enableCrashReporting,
        onCheckedChange = updateCrashReporting,
        title = stringResource(R.string.crash_reporting_checkbox_title)
    )
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
