package com.capyreader.app.ui.fixtures

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@Composable
fun InjectedCapyTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current

    KoinApplication(
        application = {
            androidContext(context)
            setupCommonModules()
        }
    ) {
        CapyTheme {
            content()
        }
    }
}
