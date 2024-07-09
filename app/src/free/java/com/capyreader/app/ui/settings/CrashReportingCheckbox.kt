package com.capyreader.app.ui.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun CrashReportingCheckbox() {
   Text("Stub")
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
