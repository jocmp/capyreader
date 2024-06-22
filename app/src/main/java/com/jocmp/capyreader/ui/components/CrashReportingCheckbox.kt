package com.jocmp.capyreader.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.jocmp.capyreader.R
import com.jocmp.capyreader.common.AppPreferences
import com.jocmp.capyreader.setupCommonModules
import com.jocmp.capyreader.ui.theme.CapyTheme
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = enableCrashReporting, onCheckedChange = updateCrashReporting)
        Text(text = stringResource(R.string.crash_reporting_checkbox_title))
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
