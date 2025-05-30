package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.LocalContext
import com.capyreader.app.setupCommonModules
import com.capyreader.app.ui.settings.sharing.ReadeckLoginView
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

@Composable
fun SharingPanel() {
    // if has-key readeck
//    readeckLogin.isValid => show edit button, deactivate
    ReadeckLoginView()
}

@Composable
private fun SharingOptionDialog(
    form: @Composable () -> Unit,
) {

//    readeckLogin.isValid => show edit button
}

@Preview
@Composable
fun SharingPanelPreview() {
    val context = LocalContext.current

    KoinApplication(
        application = {
            androidContext(context)
            setupCommonModules()
        }
    ) {
        SharingPanel()
    }
}
