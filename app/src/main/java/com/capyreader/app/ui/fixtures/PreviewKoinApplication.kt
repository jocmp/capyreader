package com.capyreader.app.ui.fixtures

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.capyreader.app.setupCommonModules
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.compose.KoinContext
import org.koin.mp.KoinPlatformTools

@Composable
fun PreviewKoinApplication(content: @Composable () -> Unit) {
    val context = LocalContext.current

    val koinApplication = KoinPlatformTools.defaultContext().getOrNull()

    if (koinApplication != null) {
        KoinContext(koinApplication, content)
    } else {

        KoinApplication(
            application = {
                androidContext(context)
                setupCommonModules()
            },
            content,
        )
    }
}
