package com.capyreader.app.ui.settings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import com.jocmp.capy.common.launchUI

val LocalSnackbarHost = compositionLocalOf { SnackbarHostState() }

@Composable
fun localSnackbarDisplay(): (message: String) -> Unit {
    val snackbar = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()

    return { message: String ->
        scope.launchUI {
            snackbar.showSnackbar(message)
        }
    }
}
