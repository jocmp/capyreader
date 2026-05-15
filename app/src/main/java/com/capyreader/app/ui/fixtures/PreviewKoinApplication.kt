package com.capyreader.app.ui.fixtures

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.capyreader.app.setupCommonModules
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import org.koin.mp.KoinPlatformTools

@Composable
fun PreviewKoinApplication(content: @Composable () -> Unit) {
    val context = LocalContext.current

    if (KoinPlatformTools.defaultContext().getOrNull() != null) {
        content()
    } else {
        KoinApplication(
            configuration = koinConfiguration {
                androidContext(context)
                setupCommonModules()
            },
            content = content,
        )
    }
}
