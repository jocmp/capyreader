package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun FullscreenVideoDialog(
    onDismissRequest: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window

        LaunchedEffect(dialogWindow) {
            if (dialogWindow != null) {
                WindowCompat.setDecorFitsSystemWindows(dialogWindow, false)

                val insets = WindowCompat.getInsetsController(dialogWindow, dialogWindow.decorView)
                insets.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insets.hide(WindowInsetsCompat.Type.systemBars())
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            content = content,
        )
    }
}
